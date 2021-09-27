package app.example.myquizapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static app.example.myquizapp.SetsActivity.setIDs;
import static app.example.myquizapp.SplashActivity.catList;
import static app.example.myquizapp.SplashActivity.selectedCategoryIndex;

public class QuestionActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView question, qCount, timer;
    private Button option1, option2, option3, option4;
    private List<Question> questionList;
    private int quesNum;
    private CountDownTimer countDown;
    private int score;
    private FirebaseFirestore firestore;
    private int setNo;
    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        question = findViewById(R.id.question);
        qCount = findViewById(R.id.questionNumber);
        timer = findViewById(R.id.countdown);

        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);

        option1.setOnClickListener(this);
        option2.setOnClickListener(this);
        option3.setOnClickListener(this);
        option4.setOnClickListener(this);

        loadingDialog = new Dialog(QuestionActivity.this);
        loadingDialog.setContentView(R.layout.loading_progressbar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.show();

        questionList = new ArrayList<>();

        setNo = getIntent().getIntExtra("SETNO",1);

        firestore = FirebaseFirestore.getInstance();

        getQuestionsList();

        score = 0;
    }

    private void getQuestionsList() {

        questionList.clear();

        loadingDialog.show();

        firestore.collection("quiz").document(catList.get(selectedCategoryIndex).getId())
                .collection(setIDs.get(setNo)).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        Map<String, QueryDocumentSnapshot> docList = new ArrayMap<>();

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots){

                            docList.put(doc.getId(), doc);
                        }

                        QueryDocumentSnapshot quesListDoc = docList.get("QUESTIONS_LIST");

                        String count = quesListDoc.getString("COUNT");

                        for (int i=0; i<Integer.valueOf(count); i++){

                            String quesID = quesListDoc.getString("Q" + String.valueOf(i+1) + "_ID");

                            QueryDocumentSnapshot quesDoc = docList.get(quesID);//this query will get the questions one by one.

                            questionList.add(new Question(
                                    quesDoc.getString("QUESTION"),
                                    quesDoc.getString("A"),
                                    quesDoc.getString("B"),
                                    quesDoc.getString("C"),
                                    quesDoc.getString("D"),
                                    Integer.valueOf(quesDoc.getString("ANSWER"))
                            ));
                        }

                        setQuestion();

                        loadingDialog.dismiss();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingDialog.dismiss();
                Toast.makeText(QuestionActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void setQuestion() {
        timer.setText(String.valueOf(10));

        question.setText(questionList.get(0).getQuestion());
        option1.setText(questionList.get(0).getOptionA());
        option2.setText(questionList.get(0).getOptionB());
        option3.setText(questionList.get(0).getOptionC());
        option4.setText(questionList.get(0).getOptionD());

        qCount.setText(String.valueOf(1)+ "/" + String.valueOf(questionList.size()));

        startTimer();

        quesNum = 0;
    }

    private void startTimer() {
        countDown = new CountDownTimer(12000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (millisUntilFinished<10000){
                    timer.setText(String.valueOf(millisUntilFinished / 1000));
                }
            }

            @Override
            public void onFinish() {
                changeQuestion();

            }
        };
        countDown.start();
    }



    @Override
    public void onClick(View view) {

        int selectedOption = 0;

        switch (view.getId()){
            case R.id.option1:
                selectedOption = 1;
                break;
            case R.id.option2:
                selectedOption = 2;
                break;
            case R.id.option3:
                selectedOption = 3;
                break;
            case R.id.option4:
                selectedOption = 4;
                break;
            default:
        }
        countDown.cancel();

        checkAnswer(selectedOption, view);

    }

    private void checkAnswer(int selectedOption, View view) {
        if (selectedOption == questionList.get(quesNum).getCorrectAns()){
            //right answer
            ((Button)view).setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
            score++;
        }else{
            //wrong answer
            ((Button)view).setBackgroundTintList(ColorStateList.valueOf(Color.RED));

            switch (questionList.get(quesNum).getCorrectAns()){
                case 1:
                    option1.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                    break;
                case 2:
                    option2.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                    break;
                case 3:
                    option3.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                    break;
                case 4:
                    option4.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                    break;
            }
        }
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                changeQuestion();
            }
        }, 2000);


    }

    private void changeQuestion() {
        if (quesNum < questionList.size()-1){

            quesNum++;//while calling next question first increment question

            //method to go to next question with animation...
            playAnim(question,0,0);
            playAnim(option1,0, 1);
            playAnim(option2,0, 2);
            playAnim(option3,0, 3);
            playAnim(option4,0, 4);

            //change the question number
            qCount.setText(String.valueOf(quesNum+1) + "/" + String.valueOf(questionList.size()));

            //to reset timer
            timer.setText(String.valueOf(10));
            startTimer();


        }else{
            //it is last question..go to score activity...
            Intent intent = new Intent(QuestionActivity.this,ScoreActivity.class);
            intent.putExtra("SCORE",String.valueOf(score) + "/" +String.valueOf(questionList.size()));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//clears all the previous activity
            startActivity(intent);
            //QuestionActivity.this.finish();
        }
    }

    private void playAnim(final View view, final int value, final int viewNum) {

        //animation of the buttons...
        view.animate().alpha(value).scaleX(value).scaleX(value).setDuration(500)
                .setStartDelay(100).setInterpolator(new DecelerateInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        if (value == 0){
                            switch (viewNum){
                                case 0:
                                    ((TextView)view).setText(questionList.get(quesNum).getQuestion());
                                    break;
                                case 1:
                                    ((Button)view).setText(questionList.get(quesNum).getOptionA());
                                    break;
                                case 2:
                                    ((Button)view).setText(questionList.get(quesNum).getOptionB());
                                    break;
                                case 3:
                                    ((Button)view).setText(questionList.get(quesNum).getOptionC());
                                    break;
                                case 4:
                                    ((Button)view).setText(questionList.get(quesNum).getOptionD());
                                    break;
                            }

                            if (viewNum != 0){
                                ((Button)view).setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E99C03")));
                            }

                            playAnim(view, 1, viewNum);

                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
    }
    //stops the timer on back press...
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        countDown.cancel();
    }
}