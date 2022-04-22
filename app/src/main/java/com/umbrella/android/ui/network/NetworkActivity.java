package com.umbrella.android.ui.network;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.umbrella.android.R;
import com.umbrella.android.data.Const;
import com.umbrella.android.data.NetworkDataSource;
import com.umbrella.android.data.db.DataBase;
import com.umbrella.android.data.db.DataBaseHelper;
import com.umbrella.android.data.neuralNetwork.network.Network;
import com.umbrella.android.data.neuralNetwork.pictureService.Serialization;
import com.umbrella.android.databinding.ActivityNetworkBinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NetworkActivity extends AppCompatActivity {

    private Validation validation;
    private ActivityNetworkBinding binding;
    private EditText numberHiddenEditText;
    private EditText numberCycleEditText;
    private EditText learningRateEditText;
    private EditText errorEditText;
    private Button imageButton;
    private Button recognizeButton;
    private String numberHidden;
    private String numberCycle;
    private String learningRate;
    private String error;
    private static final String IRPROP = "IRProp";
    private static final String RPROP = "RProp";
    private static final String BACKPROP = "BackProp";
    private static String flagAlgorithm;

    public static String getFlagAlgorithm() {
        return flagAlgorithm;
    }

    public static List<double[]> getPatterns() {
        return patterns;
    }

    private static List<double[]> patterns = new ArrayList<>();

    public static ImageView getImageForRecognize() {
        return imageForRecognize;
    }

    public static ImageView getImageForView() {
        return imageForView;
    }

    private SQLiteDatabase mDb =null;
    private DataBaseHelper dataBaseHelper;

    private static ImageView imageForView;
    private static ImageView imageForRecognize;
    private RadioGroup radioGroup;
    private final int pick_image = 1;

    private void init() throws SQLException, ClassNotFoundException, IOException {
        dataBaseHelper = new DataBaseHelper(this);
        mDb = dataBaseHelper.getWritableDatabase();
        if(mDb != null){
            System.out.println("К бд подключились");
        }
        numberHiddenEditText = binding.filedNumberHidden;
        numberCycleEditText = binding.filedNumberCycle;
        learningRateEditText = binding.filedCoeffStudy;
        errorEditText = binding.filedError;
        recognizeButton = binding.recognizeButton;
        radioGroup = binding.radioGroup;
        imageButton = binding.imageButtonUpload;
        imageForRecognize = binding.imageButton;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Serialization serialization = new Serialization(this);
        binding = ActivityNetworkBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        validation = new ViewModelProvider(this, new NetworkViewModelFactory())
                .get(Validation.class);

        try {
            init();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        Validation.getLoginFormState().observe(this, new Observer<NetworkFormState>() {
            @Override
            public void onChanged(@Nullable NetworkFormState networkFormState) {
                if (networkFormState == null) {
                    return;
                }
                recognizeButton.setEnabled(networkFormState.isDataValid());
                if (networkFormState.getNumberHiddenError() != null) {
                    numberHiddenEditText.setError(getString(networkFormState.getNumberHiddenError()));
                }
                if (networkFormState.getNumberCycleError() != null) {
                    numberCycleEditText.setError(getString(networkFormState.getNumberCycleError()));
                }
                if (networkFormState.getLearningRate() != null) {
                    learningRateEditText.setError(getString(networkFormState.getLearningRate()));
                }
                if (networkFormState.getError() != null) {
                    errorEditText.setError(getString(networkFormState.getError()));
                }
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                validation.NetworkDataChanged(numberHiddenEditText.getText().toString().trim(),
                        numberCycleEditText.getText().toString().trim(),
                        learningRateEditText.getText().toString().trim(),
                        errorEditText.getText().toString().trim());
            }
        };

        numberHiddenEditText.addTextChangedListener(afterTextChangedListener);
        learningRateEditText.addTextChangedListener(afterTextChangedListener);
        numberCycleEditText.addTextChangedListener(afterTextChangedListener);
        errorEditText.addTextChangedListener(afterTextChangedListener);

        binding.buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (NetworkDataSource.getNetwork() == null) {
                    openSiteDialog();
                } else {
                    // dataBaseHelper.Save();

                    Network network = NetworkDataSource.getNetwork();
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();

                    try {
                        ObjectOutputStream o = new ObjectOutputStream(bos);
                        System.out.println("Сохранение 1/2");
                        o.writeObject(network);
                        byte[] b = bos.toByteArray();
                        String sql = "Insert into Network(id, network) Values('" + b + "');";
                        mDb.execSQL(sql);
                        System.out.println("Сохранение 2/2");
                        // Cursor cursor = "Insert into Network(id, network) Values(" + k + ",'" + b + "');";
                        //TODO фигачь тут БД
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }


            }
        });
        binding.buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* if (NetworkDataSource.getNetwork() == null) {
                    openSiteDialog();
                } else {
                    try {
                        dataBase.SelectAll();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                    //TODO фигачь тут БД
                }*/
            }
        });
        binding.buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /*try {
                    //dataBase.Update();
                } catch (SQLException | ClassNotFoundException throwables) {
                    throwables.printStackTrace();
                }*/
                //TODO фигачь тут БД
            }
        });
        binding.buttonCreateNetwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readInputValuesForNetwork();
                if ((!numberHidden.equals("") && !numberCycle.equals("") && !learningRate.equals("") && !error.equals("")) ||
                        (!numberHidden.equals("") && !numberCycle.equals("") && !learningRate.equals("")) ||
                        (!numberHidden.equals("") && !numberCycle.equals("") && !error.equals("")) || (
                        !numberHidden.equals("") && !numberCycle.equals("")) ||
                        (!numberHidden.equals("") || !numberCycle.equals("") || !learningRate.equals(""))) {
                    NetworkDataSource networkDataSource = new NetworkDataSource();
                    networkDataSource.network(numberHidden, numberCycle, learningRate, error);
                    openSiteDialogCreateNetwork();
                } else {
                    openSiteDialog();
                }
            }
        });

        binding.buttonRestudy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkDataSource.getNetwork() == null) {
                    openSiteDialog();
                } else {
                    readInputValuesForNetwork();
                    String str = "1";
                    for (int i = 0; i < Const.NUMBER_OUTPUT_NEURONS; i++) {
                        try {
                            double[] temp = serialization.readValuesForTraining(Const.ARRAY_LETTERS[i]);
                            if (temp != null) {
                                patterns.add(i, temp);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    for (int i = 0; i < Const.NUMBER_OUTPUT_NEURONS; i++) {
                        try {
                            double[] temp = serialization.readValuesForTraining(Const.ARRAY_LETTERS[i] + str);
                            if (temp != null) {
                                patterns.add(i, temp);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    //serialization.readImage(imageView.getTag().toString());
                    try {
                        NetworkDataSource.restudy(numberHidden, numberCycle, learningRate, error);
                    } catch (IOException | URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }

        });

        numberHiddenEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    validation.network(numberHiddenEditText.getText().toString().trim(),
                            numberCycleEditText.getText().toString().trim(),
                            learningRateEditText.getText().toString().trim(),
                            errorEditText.getText().toString().trim());
                } else {
                    return false;
                }
                return true;
            }
        });
        binding.switchLenguige.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Locale locale = new Locale("value_en");
                    changeLocaleOnEnglish(locale);
                } else {
                    Locale locale = new Locale("value_ru");
                    changeLocaleOnRussian(locale);
                }
            }
        });
        recognizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if (isImage(imageView)) {
                if (NetworkActivity.getFlagAlgorithm() == null || imageForRecognize == null
                        || NetworkDataSource.getNetwork() == null) {
                    openSiteDialog();
                } else {
                    readInputValuesForNetwork();
                    String str = "1";
                    for (int i = 0; i < Const.NUMBER_OUTPUT_NEURONS; i++) {
                        try {
                            double[] temp = serialization.readValuesForTraining(Const.ARRAY_LETTERS[i]);
                            if (temp != null) {
                                patterns.add(i, temp);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    for (int i = 0; i < Const.NUMBER_OUTPUT_NEURONS; i++) {
                        try {
                            double[] temp = serialization.readValuesForTraining(Const.ARRAY_LETTERS[i] + str);
                            if (temp != null) {
                                patterns.add(i, temp);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    //   serialization.readImageForView();
                    //serialization.readImage(imageView.getTag().toString());
                    serialization.readImageForTesting();
                    try {
                        NetworkDataSource.initDataAndChooseAlgorithm();
                    } catch (IOException | URISyntaxException e) {
                        e.printStackTrace();
                    }

                    TextView textView = findViewById(R.id.Answer);
                    if (Network.getAnswer() != null) {
                        StringBuffer sb = new StringBuffer(textView.getText());
                        textView.setText(sb.delete(textView.getText().length() - 1, textView.getText().length()));
                        textView.setText(textView.getText() + " " + Network.getAnswer());
                    }
                }
                //}
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onCheckedChanged(RadioGroup arg0, int id) {
                switch (id) {
                    case R.id.radioButtonIpprop:
                        flagAlgorithm = IRPROP;
                        break;
                    case R.id.radioButtonBackProp:
                        flagAlgorithm = BACKPROP;
                        break;
                    case R.id.radioButtonRprop:
                        flagAlgorithm = RPROP;
                        break;
                    default:
                        break;
                }
            }
        });
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, pick_image);
            }
        });
    }

    private void readInputValuesForNetwork() {
        numberHidden = numberHiddenEditText.getText().toString().trim();
        numberCycle = numberCycleEditText.getText().toString().trim();
        learningRate = learningRateEditText.getText().toString().trim();
        error = errorEditText.getText().toString().trim();
    }

    @SuppressLint("ResourceType")
    @SuppressWarnings("deprecation")
    private void changeLocaleOnEnglish(Locale locale) {
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.setLocale(locale);
        getBaseContext().getResources()
                .updateConfiguration(configuration,
                        getBaseContext()
                                .getResources()
                                .getDisplayMetrics());
        setTitle(R.string.app_name);

        TextView tv = (TextView) findViewById(R.id.recognizeButton);
        tv.setText("Recognize a letter");
        tv = (TextView) findViewById(R.id.imageButtonUpload);
        tv.setText("Load image");
        tv = (TextView) findViewById(R.id.buttonSave);
        tv.setText("Save");
        tv = (TextView) findViewById(R.id.buttonDelete);
        tv.setText("Delete");
        tv = (TextView) findViewById(R.id.buttonUpload);
        tv.setText("Upload");
        tv = (TextView) findViewById(R.id.buttonRestudy);
        tv.setText("Retrain");
        tv = (TextView) findViewById(R.id.textViewFiledCoeffStudy);
        tv.setText("Learning Rate");
        tv = (TextView) findViewById(R.id.textViewFiledNumberHidden);
        tv.setText("Number of neurons in the hidden layer");
        tv = (TextView) findViewById(R.id.textViewFiledError);
        tv.setText("Error");
        tv = (TextView) findViewById(R.id.textViewFiledNumberCycle);
        tv.setText("Number of training cycles");
        tv = (TextView) findViewById(R.id.Answer);
        tv.setText("Network answer: ");
        tv = (TextView) findViewById(R.id.button_create_network);
        tv.setText("Create network");
    }

    @SuppressLint("ResourceType")
    @SuppressWarnings("deprecation")
    private void changeLocaleOnRussian(Locale locale) {
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.setLocale(locale);
        getBaseContext().getResources()
                .updateConfiguration(configuration,
                        getBaseContext()
                                .getResources()
                                .getDisplayMetrics());
        setTitle(R.string.app_name);

        TextView tv = (TextView) findViewById(R.id.recognizeButton);
        tv.setText("Распознать букву");
        tv = (TextView) findViewById(R.id.imageButtonUpload);
        tv.setText("Загрузить изображение");
        tv = (TextView) findViewById(R.id.buttonSave);
        tv.setText("Сохранить");
        tv = (TextView) findViewById(R.id.buttonDelete);
        tv.setText("Удалить");
        tv = (TextView) findViewById(R.id.buttonUpload);
        tv.setText("Загрузить");
        tv = (TextView) findViewById(R.id.buttonRestudy);
        tv.setText("Переобучить");
        tv = (TextView) findViewById(R.id.textViewFiledCoeffStudy);
        tv.setText("Коэффициент обучения");
        tv = (TextView) findViewById(R.id.textViewFiledNumberHidden);
        tv.setText("Количество нейронов в скрытом слое");
        tv = (TextView) findViewById(R.id.textViewFiledError);
        tv.setText("Погрешность обучения");
        tv = (TextView) findViewById(R.id.textViewFiledNumberCycle);
        tv.setText("Количество циклов обучения");
        tv = (TextView) findViewById(R.id.Answer);
        tv.setText("Ответ сети: ");
        tv = (TextView) findViewById(R.id.button_create_network);
        tv.setText("Создать нейронную сеть");
    }

    private void openSiteDialog() {
        SpannableString webaddress = null;
        if (NetworkActivity.getFlagAlgorithm() == null) {
            webaddress = new SpannableString(
                    "Выберите алгоритм обучения");
            Linkify.addLinks(webaddress, Linkify.ALL);
        } else if (imageForRecognize == null) {
            webaddress = new SpannableString(
                    "Загрузите изображение");
            Linkify.addLinks(webaddress, Linkify.ALL);
        } else if ((numberHidden.equals("") && numberCycle.equals("") && learningRate.equals("") && error.equals("")) ||
                (numberHidden.equals("") && numberCycle.equals("") && learningRate.equals("")) ||
                (numberHidden.equals("") && numberCycle.equals("") && error.equals("")) || (
                numberHidden.equals("") && numberCycle.equals("")) ||
                (numberHidden.equals("") || numberCycle.equals("") || learningRate.equals(""))) {
            webaddress = new SpannableString(
                    "Введите данные для создания нейронной сети");
            Linkify.addLinks(webaddress, Linkify.ALL);
        } else if (NetworkDataSource.getNetwork() == null) {
            webaddress = new SpannableString(
                    "Создайте нейронную сеть");
            Linkify.addLinks(webaddress, Linkify.ALL);
        }
        final AlertDialog aboutDialog = new AlertDialog.Builder(
                NetworkActivity.this).setMessage(webaddress)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).create();

        aboutDialog.show();

        ((TextView) aboutDialog.findViewById(android.R.id.message))
                .setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void openSiteDialogCreateNetwork() {
        SpannableString webaddress = new SpannableString(
                "Нейронная сеть создана");
        Linkify.addLinks(webaddress, Linkify.ALL);
        final AlertDialog aboutDialog = new AlertDialog.Builder(
                NetworkActivity.this).setMessage(webaddress)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).create();

        aboutDialog.show();

        ((TextView) aboutDialog.findViewById(android.R.id.message))
                .setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent
            imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch (requestCode) {
            case pick_image:
                if (resultCode == RESULT_OK) {
                    try {
                        final Uri imageUri = imageReturnedIntent.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        imageForView.setImageBitmap(selectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
        }
    }

    public boolean isImage(ImageView imageView) {
        return Validation.networkImage(imageView);
    }

}