package com.bignerdranch.android.passwordgenerator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener {

    private MyRecyclerViewAdapter adapter;  //класс адаптера в отдельном файле

    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String PUNCTUATION = "!@#$%&*()_+-=[]|,./?><";
    private ArrayList<String> generatedPasswords;
    private CheckBox mNumbersCheckBox;
    private CheckBox mLettersCheckBox;
    private CheckBox mBigLettersCheckBox;
    private CheckBox mPunctuationsCheckBox;
    private TextView passwords;
    private RecyclerView recyclerView;
    private final String mString = "Сгенерированные пароли(пароль будет скопирован при нажатии):";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNumbersCheckBox = findViewById(R.id.nums);
        mLettersCheckBox = findViewById(R.id.letters);
        mBigLettersCheckBox = findViewById(R.id.big_letters);
        mPunctuationsCheckBox = findViewById(R.id.punctuations);
        passwords = findViewById(R.id.passwords);

        generatedPasswords = new ArrayList<>(99);

        if (savedInstanceState != null) { //при повороте экрана выводим текст и восстанавливаем arraylist
            passwords.setText(mString);
            generatedPasswords.addAll(Objects.requireNonNull(savedInstanceState.getStringArrayList("GEN_PASS")));
        }

        // set up the RecyclerView
        recyclerView = findViewById(R.id.password_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyRecyclerViewAdapter(this, generatedPasswords);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

    }

    public void generatePassword(View view) { //при нажатии на кнопку

        recyclerView.removeAllViews(); //очищаем список

        if (!(mNumbersCheckBox.isChecked() || mLettersCheckBox.isChecked() ||
                mBigLettersCheckBox.isChecked() || mPunctuationsCheckBox.isChecked())) { //если ни один из пунктов не выбран
            Toast.makeText(this, "Выберите хотя бы один пункт!", Toast.LENGTH_LONG).show();
            return;
        }

        EditText passwordLength = findViewById(R.id.pass_length);
        if (passwordLength.getText().length() == 0) {
            Toast.makeText(this, "Введите корректную длину пароля!", Toast.LENGTH_LONG).show();
            return;
        }

        int passLength = Integer.parseInt(passwordLength.getText().toString());
        if (passLength == 0) {
            passwordLength.setText("");
            Toast.makeText(this, "Длина пароля не может быть равна 0!", Toast.LENGTH_LONG).show();
            return;
        }

        EditText howManyPass = findViewById(R.id.how_many_pass);
        if (howManyPass.getText().length() == 0) { // если не задано кол-во паролей задаем 2
            howManyPass.setText("2");
        }

        int numberOfPass = Integer.parseInt(howManyPass.getText().toString());
        if (numberOfPass == 0) {    // если кол-во паролей задано нулем, то очищаем edittext
            howManyPass.setText("");
            Toast.makeText(this, "Количество паролей не может быть равно 0!", Toast.LENGTH_LONG).show();
            return;
        }

        passwords.setText(mString);

        generatedPasswords.clear(); //очищаем arraylist

        for (int i = 0; i < numberOfPass; i++) { //заполянем arraylist сгенерирвоанными паролями
            generatedPasswords.add(generate(passLength));
        }

        // прячем клавиатуру. view - это кнопка
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

    }

    public String generate(int length) {  //генерация пароля заданной длины

        StringBuilder password = new StringBuilder(length);
        Random random = new Random(System.nanoTime());

        List<String> charCategories = new ArrayList<>(4);

        if (mLettersCheckBox.isChecked()) {
            charCategories.add(LOWER);
        }
        if (mBigLettersCheckBox.isChecked()) {
            charCategories.add(UPPER);
        }
        if (mNumbersCheckBox.isChecked()) {
            charCategories.add(DIGITS);
        }
        if (mPunctuationsCheckBox.isChecked()) {
            charCategories.add(PUNCTUATION);
        }

        for (int i = 0; i < length; i++) {
            String charCategory = charCategories.get(random.nextInt(charCategories.size()));
            int position = random.nextInt(charCategory.length());
            password.append(charCategory.charAt(position));
        }
        return new String(password);
    }


    //при нажатии на элемент списка копируем пароль и выводим тост
    @Override
    public void onItemClick(View view, int position) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("", generatedPasswords.get(position));
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Пароль скопирован", Toast.LENGTH_SHORT).show();
    }

    //сохраняем arraylist
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putStringArrayList("GEN_PASS", generatedPasswords);
        super.onSaveInstanceState(outState);
    }

}
