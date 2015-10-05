package com.example.hashimotomika.tweetforwear;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class EditPhoneNumberActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_phone_number);
        Button button = (Button) findViewById(R.id.submit_phone_number);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button button = (Button) v;
                EditText text = (EditText) findViewById(R.id.edit_phone_number);
                String phoneNumber = text.getText().toString();
                if (phoneNumber != "") {
                    setPhoneNumber(phoneNumber);
                } else {
                    showMessage("input phone number");
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setPhoneNumber(String phoneNumber) {
        CallRequest.storePhoneNumber(this, phoneNumber);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

}
