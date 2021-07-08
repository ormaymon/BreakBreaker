package syntax.org.il.gameproject;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    //ImageView platform , ball;
    //View leftBorder,rightBorder, topBorder,bottomBorder;

    //float newX ,speed = 1;
    //float ballX , ballY,ballZ;

    /*float scale = getResources().getDisplayMetrics().density;*/
    /*Rect rightBorderRect = new Rect() , leftBorderRect = new Rect() , topBorderRect = new Rect(),bottomBorderRect = new Rect();
    Rect ballRect = new Rect();
    Rect platformRectR  =new Rect() , platformRectL = new Rect();*/


    //Defining all the Variables
    SensorManager sensorManger;
    private Sensor sensorAccel;
    float ballMovementX, ballMovementY, speedx, speedY;
    int platMovementX;
    int scale;
    GameView gameView;
    Brick[] bricks;
    Rect brick = new Rect();
    int indexOfBrick = 0;
    Ball gameBall;
    Brick platform;
    float angleX;
    float angleY;
    float angle;
    boolean up = true;
    boolean right = true;
    boolean startGame = false;
    int screenX, screenY;
    Display display;
    Point size = new Point();
    int bricksDestroyed = 0;
    Handler handler = new Handler();
    View dialogView;
    AlertDialog.Builder builder;
    AlertDialog lvlComplete;


    boolean paused = false;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        scale = (int) getResources().getDisplayMetrics().density;
        angle = (float) Math.PI;
        angleX = (float) (Math.cos(angle / 18));
        angleY = (float) (Math.sin(angle / 18));
        speedx = 7 * scale;
        speedY = 7 * scale;
        ballMovementX = speedx;
        ballMovementY = speedY;


        gameView = (GameView) findViewById(R.id.game_view);
        display = getWindowManager().getDefaultDisplay();
        display.getSize(size);
        screenX = size.x;
        screenY = size.y;

        bricks = gameView.createMatrix(1);
        //gameBall = gameView.createCircle(180*scale,500*scale,4*scale);
        //platform = gameView.createPlatform(200*scale,545*scale,270*scale,555*scale);
        platform = gameView.createPlatform((screenX / 2), (screenY - 50 * scale), (screenX / 2 + 50 * scale), (screenY - 40 * scale));
        gameBall = gameView.createCircle(platform.getLeft() + (platform.getRight() - platform.getLeft()) / 2, platform.getTop() - 20 * scale, 4 * scale);
        gameView.setBorders(screenX, screenY);
        //borders = gameView.getBorder(borders);
        //brickBoxes = new Rect[bricks.length];


        sensorManger = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorAccel = sensorManger.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //sensorGyro = sensorManger.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

    }


    @Override
    public void onSensorChanged(SensorEvent event) {


        if(!paused){
        //Platform Movement

        platMovementX = 5 * (int) (event.values[0] * scale);
        if (platform.getLeft() > 0 && platMovementX > 0) {
            gameView.movePlatform(platform, platMovementX);
            if (!startGame) {
                gameView.moveCircle(gameBall, -platMovementX, 0);
            }
        }

        if (platform.getRight() < 350 * scale && platMovementX < 0) {
            gameView.movePlatform(platform, platMovementX);
            if (!startGame) {
                gameView.moveCircle(gameBall, -platMovementX, 0);
            }
        }


        if (startGame) {
            gameView.moveCircle(gameBall, ballMovementX, ballMovementY);

            //Ball Movement
            //Hits left side
            if (gameBall.getCenterX() < 0) {
                right = true;
                if (up) {
                    setAngles(1);
                    ballMovementY = -1 * speedY * angleY;
                } else if (!up) {
                    setAngles(3);
                    ballMovementY = -1 * speedY * angleY;
                }
                ballMovementX = speedx * angleX;
            }
            //Hits top
            if (gameBall.getCenterY() < 0) {
                up = false;
                if (right) {
                    setAngles(3);
                } else if (!right) {
                    setAngles(4);
                }
                ballMovementX = speedx * angleX;
                ballMovementY = -1 * speedY * angleY;
            }
            //Hits right side
            if (gameBall.getCenterX() > screenX) {
                right = false;
                if (up) {
                    setAngles(2);
                    ballMovementY = -1 * speedY * angleY;
                } else if (!up) {
                    setAngles(4);
                    ballMovementY = -1 * speedY * angleY;
                }
                ballMovementX = speedx * angleX;
                if (ballMovementX > 0) {
                    ballMovementX = -ballMovementX;
                }
            }

            //Hits Bottom and Loses life
            if (gameBall.getCenterY() > screenY) {
                //ballMovementY = -ballMovementY;
                gameView.loseLife();
                gameBall = gameView.createCircle(platform.getLeft() + (platform.getRight() - platform.getLeft()) / 2, platform.getTop() - 20 * scale, 4 * scale);
                startGame = false;
                ballMovementX = speedx;
                ballMovementY = speedY;
            }

            if (gameBall.hitsPlatform(platform) == 1) {
                up = true;
                if (right) {
                    setAngles(1);

                } else if (!right) {
                    setAngles(2);
                }
                ballMovementX = speedx * angleX;
                ballMovementY = -1 * speedY * angleY;
            }

            for (Brick brick : bricks) {
                if (gameBall.hitsBrick(brick)) {
                    bricksDestroyed++;
                    up = !up;
                    right = !right;
                    ballMovementX = -ballMovementX;
                    ballMovementY = -ballMovementY;
                    brick.set(0, 0, 0, 0, 0);
                    //brick.setRectF();
                    bricks = gameView.deleteBrick(bricks, brick);
                    break;
                    //index = 0;
                }
            }
            if (bricks.length == bricksDestroyed) {
                paused = true;
                levelUp();
            }

        }


    }

}

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN)
        {
            startGame = true;
        }

        return super.onTouchEvent(event);



    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManger.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(sensorAccel != null){
            sensorManger.registerListener(this , sensorAccel , SensorManager.SENSOR_DELAY_GAME);
        }
    }

    float upRightAngle(){
        int n =  (int)(Math.random() *3) +1;
        switch (n){
            case 1:
                return (float)Math.PI/6;
            case 2:
                return(float)Math.PI/4;
            case 3:
                return (float)Math.PI/3;
        }
        return 0;
    }

    float upLeftAngle(){
        int n = (int)(Math.random() * 3) +1;
        switch (n){
            case 1:
                return 2 * (float)Math.PI/3;
            case 2:
                return 3* (float)Math.PI/4;
            case 3:
                return 5 * (float)Math.PI/6;
        }
        return 0;
    }

    float downRightAngle(){
        int n = (int)(Math.random()*3) +1;
        switch (n){
            case 1:
                return 5 * (float)Math.PI/3;
            case 2:
                return 7* (float)Math.PI/4;
            case 3:
                return 11 * (float)Math.PI/6;
        }
        return 0;
    }

    float downLeftAngle(){
        int n = (int)(Math.random()*3) +1;
        switch (n){
            case 1:
                return 4 * (float)Math.PI/3;
            case 2:
                return 5* (float)Math.PI/4;
            case 3:
                return 7 * (float)Math.PI/6;
        }
        return 0;
    }


    void setAngles(int type){
        switch (type){
            case 1:
                angle = upRightAngle();
                break;
            case 2:
                angle = upLeftAngle();
                break;
            case 3:
                angle = downRightAngle();
                break;
            case 4:
                angle = downLeftAngle();
                break;
        }
        angleX = (float)(Math.cos(angle));
        angleY = (float)(Math.sin(angle));
    }

    void levelUp(){

        handler.post(new Runnable() {
            @Override
            public void run() {
                builder = new AlertDialog.Builder(MainActivity.this,R.style.Theme_AppCompat_Dialog_Alert);
                dialogView = getLayoutInflater().inflate(R.layout.level_complete,null);
                Button nextLvlBtn = dialogView.findViewById(R.id.next_level);
                Button restartLvlBtn = dialogView.findViewById(R.id.restart_level);
                builder.setView(dialogView).setCancelable(false);
                lvlComplete = builder.create();
                lvlComplete.show();


                nextLvlBtn.setOnClickListener(new AlertDialogsOnClickListener());
                restartLvlBtn.setOnClickListener(new AlertDialogsOnClickListener());
            }
        });

    }

    public class AlertDialogsOnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            gameBall = gameView.createCircle(platform.getLeft() + (platform.getRight() - platform.getLeft()) / 2, platform.getTop() - 20 * scale, 4 * scale);
            paused = false;
            startGame = false;
            switch(v.getId()){
                case R.id.next_level:
                    bricksDestroyed = 0;
                    bricks = gameView.createMatrix(1);
                    lvlComplete.dismiss();
                    break;
                case R.id.restart_level:
                    bricksDestroyed = 0;
                    bricks = gameView.createMatrix(0);
                    lvlComplete.dismiss();
                    break;
            }
        }
    }

}





//...........................................................................................//

//Old on Create Code
/*platform = findViewById(R.id.platform_iv);
        leftBorder = findViewById(R.id.left_border_tv);
        rightBorder = findViewById(R.id.right_border_tv);
        topBorder = findViewById(R.id.top_border_tv);
        bottomBorder = findViewById(R.id.bottom_brder_tv);
        ball = findViewById(R.id.ball_iv);


       // GameView gameView;
        gameView = findViewById(R.id.game_view);
        //bricks = new Rect[100];
        bricks = gameView.createMatrix(5, 5);






        leftBorderRect.set(leftBorder.getLeft() ,leftBorder.getTop() , leftBorder.getRight() , leftBorder.getBottom());
        rightBorderRect.set(rightBorder.getLeft() , rightBorder.getTop() , rightBorder.getRight() , rightBorder.getBottom());
        topBorderRect.set(topBorder.getLeft(), topBorder.getTop(),topBorder.getRight(),topBorder.getBottom());
        bottomBorderRect.set(bottomBorder.getLeft(),bottomBorder.getTop(),bottomBorder.getRight(),bottomBorder.getBottom());
        platformRectL.set(platform.getLeft() , platform.getTop(),platform.getWidth()/2,platform.getBottom());
        platformRectR.set(platform.getWidth()/2,platform.getTop(),platform.getRight(),platform.getBottom());

        ballRect.set(ball.getLeft() , ball.getTop() , ball.getRight() , ball.getBottom());*/

//..................................................................................................................//


//Old Sensor Change Code
/*{




            ballRect.set(ball.getLeft() , ball.getTop() , ball.getRight() , ball.getBottom());


            newX = 5 * event.values[0];

            ball.getHitRect(ballRect);
            platform.getHitRect(platformRectR);
            platform.getHitRect(platformRectL);
            leftBorder.getHitRect( leftBorderRect);
            rightBorder.getHitRect(rightBorderRect);
            topBorder.getHitRect(topBorderRect);
            bottomBorder.getHitRect(bottomBorderRect);



            ball.setX(ball.getX() + ballMovementX);
            ball.setY(ball.getY() + ballMovementY);



            //ball movement
            if(Rect.intersects(ballRect , rightBorderRect)){

                //ball.setX(ball.getX());
                ballMovementX = -ballMovementX;
                ball.setX(ball.getX() - 4*ballMovementX);
                //ball.setY(ball.getY() - 50);

            }

            if( Rect.intersects(ballRect,leftBorderRect)){
                ballMovementX = -ballMovementX;
                ball.setX(ball.getX() + 4*ballMovementX);
            }


            if(Rect.intersects(platformRectR,ballRect)){
                ballMovementY = -1 * ballMovementY;
                ball.setY(ball.getY() - 50);
                if(ballMovementX < 0) {
                    ballMovementX = -ballMovementX;
                    //ball.setX(ball.getX() + 50);
                }

            }
            if(Rect.intersects(platformRectL,ballRect)){
                ballMovementY = -1 * ballMovementY;
                ball.setY(ball.getY() - 50);
                if(ballMovementX > 0) {
                    ballMovementX = -ballMovementX;
                    //ball.setX(ball.getX() - 50);
                }

            }

            if(Rect.intersects(ballRect, topBorderRect)){
                ballMovementY = -1 * ballMovementY;
                ball.setY(ball.getY() + 50);
            }

            if(Rect.intersects(ballRect,bottomBorderRect)){
                *//*ballMovementY = -1 * ballMovementY;
                ball.setY(ball.getY() - 50);*//*
                gameView.loseLife();
            }

            else {
                for(Rect brick:bricks){
                    indexOfBrick++;
                    if (Rect.intersects(ballRect, brick)) {
                        //If ball is above brick
                        if(ballRect.exactCenterY() > brick.exactCenterY()) {
                            ballMovementY = -ballMovementY;
                            ball.setY(ball.getY() + ballMovementY);
                            //ball.setY(ball.getY());

                        }
                        //If ball is below brick
                        if(ballRect.exactCenterY() < brick.exactCenterY()){
                            ballMovementY = -ballMovementY;
                            ball.setY(ball.getY() + ballMovementY);
                        }

                        //If ball hits brick from the right
                        if(ball.getX() > brick.exactCenterX()){
                            ballMovementX = -ballMovementX;
                            ball.setX(ball.getX() + ballMovementX);
                        }

                        //If ball hits brick from the left
                        if(ballRect.exactCenterX() < brick.exactCenterX()){
                            ballMovementX = -ballMovementX;
                            ball.setX(ball.getX() + ballMovementX);
                        }
                        gameView.delete(brick ,indexOfBrick);
                        brick.set(0, 0, 0, 0);
                        indexOfBrick = 0;
                    }
                }

            }


            //platform movement
            float platformX = platform.getX();


            if( (Rect.intersects(platformRectL,leftBorderRect)) && newX > 0)
                platform.setX( platformX);
            else if(Rect.intersects(platformRectR,rightBorderRect) && newX < 0){
                platform.setX( platformX);
            }

            else
                platform.setX( platformX - newX);
        }*/
//try to fix
//...............................................................................................//