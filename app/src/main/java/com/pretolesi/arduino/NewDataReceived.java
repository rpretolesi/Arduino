package com.pretolesi.arduino;

/**
 * Created by RPRETOLESI on 03/02/2015.
 */
public interface NewDataReceived
{
    public abstract void onNewDataReceived(String username, boolean available);
}
How to implement your own Listener in Android/Java


        Android, Java, Programming

        by Tseng






        Share on facebook

        Share on twitter

        Share on email

        Share on print

        More Sharing Services

        41

        When you’re developing application, there is often a need to create your own controls/widgets/classes or to extend already available ones. And in most cases, you want this control/widget to be as flexible as possible. In order to achieve this, you have to create special events, which can be handled outside of your widget. Some of the popular examples are OnClickListener and OnKeyListener. But sometimes you need Events/Listener which aren’t predefined by the Java or Android SDK. In this case, you have to create your own Listener interface. In the last post, I’ve shown three different way on how to implement Listeners in your application. Now I’ll show you how to implement your own Listeners.


        To demonstrate this, we’ll use our LoginExample activity and modify it slightly to act as a RegisterExample, where you can enter a username + password to create an account. Our goal is to create an application which will allow user to register/create a new account and while the user type his name in the user field, to check “live” if the username is already in use or not. After creating a new project, we’ll copy the main.xml and LoginExample.java file into their respective directories. The LoginExample.java will be renamed RegisterExample.java. First, we do some changes to the main.xml, in order to add a second password field and an TextView, which show us if the username is available or not, as well as changing some labels/texts. The final XML can be seen below



        ?

        01
        02
        03
        04
        05
        06
        07
        08
        09
        10
        11
        12
        13
        14
        15
        16
        17
        18
        19
        20
        21
        22
        23
        24
        25
        26
        27
        28
        29
        30
        31
        32
        33
        34
        35
        36
        37
        38
        39
        40
        41
        42
        43
        44
        45
        46
        47
        48
        49
        50
        51
        52
        53
        54
        55
        56
        57
        58
        59
        60
        61
        62
        63
        64
        65
        66
        67
        68
        69

<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
        android:orientation="vertical"
        android:layout_width="fill_parent"
        android:layout_height="fill_parent"
        >
<TextView
android:layout_width="fill_parent"
        android:layout_height="wrap_content"
        android:text="Please enter your desired username"
        />
<TextView
android:layout_width="fill_parent"
        android:layout_height="wrap_content"
        android:text="Login:"
        />
<EditText
android:id="@+id/username"
        android:singleLine="true"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content"
        />
<TextView
android:id="@+id/userstatus"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content"
        />
<TextView
android:layout_width="fill_parent"
        android:layout_height="wrap_content"
        android:text="Password:"
        />
<EditText
android:id="@+id/password"
        android:password="true"
        android:singleLine="true"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content"
        />
<TextView
android:layout_width="fill_parent"
        android:layout_height="wrap_content"
        android:text="Repeat your password:"
        />
<EditText
android:id="@+id/password2"
        android:password="true"
        android:singleLine="true"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content"
        />
<Button
android:id="@+id/register_button"
        android:text="Register"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content"
        />
<Button
android:id="@+id/cancel_button"
        android:text="Cancel"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content"
        />
<TextView
android:id="@+id/result"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content"
        />
</LinearLayout>

        If you now start the application, it should look like on the picture below. Screen1 Next, we clean up some of the code from LoginExample and remove unnecessary code as well as refactoring some variable names. The final code should look something like



        ?

        01
        02
        03
        04
        05
        06
        07
        08
        09
        10
        11
        12
        13
        14
        15
        16
        17
        18
        19
        20
        21
        22
        23
        24
        25
        26
        27
        28
        29
        30
        31
        32
        33
        34
        35
        36
        37
        38
        39
        40
        41
        42
        43
        44
        45
        46
        47
        48
        49
        50
        51
        52
        53

        package com.tseng.examples;

        import com.tseng.examples.CheckUsernameEditText.OnUsernameAvailableListener;
        import android.app.Activity;
        import android.graphics.Color;
        import android.os.Bundle;
        import android.view.View;
        import android.view.View.OnClickListener;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.TextView;

public class RegisterExample extends Activity {
    // Declare our Views, so we can access them later
    private EditText etUsername;
    private EditText etPassword;
    private EditText etPassword2;
    private Button btnRegister;
    private Button btnCancel;
    private TextView lblUserStatus;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set Activity Layout
        setContentView(R.layout.main);

        // Get the EditText and Button References
        etUsername = (EditText)findViewById(R.id.username);
        etPassword = (EditText)findViewById(R.id.password);
        etPassword2 = (EditText)findViewById(R.id.password2);
        btnRegister = (Button)findViewById(R.id.register_button);
        btnCancel = (Button)findViewById(R.id.cancel_button);
        lblUserStatus = (TextView)findViewById(R.id.userstatus);

        // Set Click Listener
        btnRegister.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // create Account
            }
        });
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close the application
                finish();
            }
        });
    }
}

So far, it looks like our LoginExample. Now the real work can begin. Our goal is to extend the EditText widget for user field and our own code to it, which will check if the username is already in use or not. First, we have to create a new class. We’ll call it CheckUsernameEditText. Once you create the application and it’s default supper class constructors, our first step is to create our own interface.



        ?

        81
        82
        83
        84

// Define our custom Listener interface
public interface OnUsernameAvailableListener {
    public abstract void onAvailableChecked(String username, boolean available);
}

Our OnUsernameAvailableListener interface is quite simple and only has defined one abstract method. This method will be called, every time the username was checked and notify the user of the widget if the name was available or not. Now that we have defined the interface, we have to create a variable which will hold the listener by adding this line to top of our CheckUsernameEditText class.



        ?

        15

        OnUsernameAvailableListener onUsernameAvailableListener = null;

        Next one, we a setter to allow others to listen to this event.



        ?

        42
        43
        44
        45

// Allows the user to set an Listener and react to the event
public void setOnUsernameAvailableListener(OnUsernameAvailableListener listener) {
        onUsernameAvailableListener = listener;
        }

        And last step is to add a small function which we will call every time to trigger the event (and we don’t have to repeat the checking code inside it every time)



        ?

        46
        47
        48
        49
        50
        51
        52
        53
        54
        55

// This function is called after the check was complete
private void OnUserChecked(String username, boolean available){
        // Check if the Listener was set, otherwise we'll get an Exception when we try to call it
        if(onUsernameAvailableListener!=null) {
        // Only trigger the event, when we have a username
        if(!TextUtils.isEmpty(username)){
        onUsernameAvailableListener.onAvailableChecked(username, available);
        }
        }
        }

        So, that was the most important code in creating your own listeners. When ever you want to trigger this event, simply call OnUserChecked(username, available); Currently however, it’s never called. So we need to listen to user input and check the file. We’ll do this, by implementing the OnKeyListener interface to our CheckUserEditText class by changing the class definition to



?

        14

public class CheckUsernameEditText extends EditText implements OnKeyListener {

    Next we have to implement the onKey method, which will be called every time the user presses a key.



            ?

            46
            47
            48
            49
            50
            51
            52
            53
            54
            55
            56
            57
            58
            59
            60
            61
            62
            63
            64
            65
            66
            67
            68
            69
            70

    @Override
    public boolean onKey(View v, int keycode, KeyEvent keyevent) {
        // We only want to handle ACTION_UP events, when user releases a key
        if(keyevent.getAction()==KeyEvent.ACTION_DOWN)
            return false;

        boolean available = true;

        // Whenever a user press a key, check if the username is available
        String username = getText().toString().toLowerCase();
        if(!TextUtils.isEmpty(username)){
            // Only perform check, if we have anything inside the EditText box
            for(int i=0; i<registeredUsers.length; i++) {
                if(registeredUsers[i].equals(username)){
                    available = false;
                    // Finish the loop, as the name is already taken
                    break;
                }
            }
            // Trigger the Event and notify the user of our widget
            OnUserChecked(username, available);
            return false;
        }
        return false;
    }

    This will check the entered text if the entered username is already available inside our registeredUsers array and set the available variables value to false if the name is already registered, otherwise leave it at it’s standard value of true. Lastly it will call OnUserChecked method, passing by username and it’s status to the listening function.

    This will cause an error, because we haven’t defined registeredUsers yet. registeredUsers in our example is a simple String[] array, which will contain a few already registered names.




            ?

            16
            17
            18
            19
            20
            21
            22
            23
            24

    final private static String[] registeredUsers = new String[] {
            // This is just a fixed List for tutorial purposes
            // in a real application you'd check this server sided or inside the database
            "tseng",
            "admin",
            "root",
            "joedoe",
            "john"
    };



    In your real application you want to get this data from a remote server, an preferences file or from an sqlite database
    Our application doesn’t receive any onKey events yet, because we haven’t registered the listener with the TextView’s onKeyListener. So we have to add this line in every of the 3 super class constructors



    ?

            1
            2

//Set KeyListener to ourself
            this.setOnKeyListener(this);

    Now our CheckUsernameEditText widget is done. How ever, if we start the application we won’t notice a difference, because we haven’t replaced the username’s EditText widget with our own one. We need to change line 17 of our main.xml into



    ?

            17

    <com.tseng.examples.CheckUsernameEditText

    as well as change RegisterExample.java



    ?

            16
            17
            18

    private EditText etUsername;
    // change into
    private CheckUsernameEditText etUsername;

    and



    ?

            32
            33
            34

    etUsername = (EditText)findViewById(R.id.username);
// change into
    etUsername = (CheckUsernameEditText)findViewById(R.id.username);

    last but not least, we have to set and define the listener to our new CheckUsernameEditText widget.



            ?

            39
            40
            41
            42
            43
            44
            45
            46
            47
            48
            49
            50
            51
            52

// Set our new Listener to the Username EditText view
            etUsername.setOnUsernameAvailableListener(new OnUsernameAvailableListener(){
        @Override
        public void onAvailableChecked(String username, boolean available) {
            // Handle the event here
            if(!available){
                etUsername.setTextColor(Color.RED);
                lblUserStatus.setText(username + " is already taken. Please choose another login name.");
            } else {
                etUsername.setTextColor(Color.GREEN);
                lblUserStatus.setText(username + " is available.");
            }
        }
    });

    The onAvailableChecked will be called every time after the check was done. We can put our code here to handle the outcome. The above’s example is pretty easy and will just change the text color of the CheckUsernameTextEdit field and update the lblUserStatus label wo notify the user his desired name is available or not. That’s all. Congratulation to your first own listener implementation. Now we can run our application. If everything worked well