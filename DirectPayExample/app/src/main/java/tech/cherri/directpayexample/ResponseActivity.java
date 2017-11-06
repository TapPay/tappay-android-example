package tech.cherri.directpayexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by Amber on 2017/1/18.
 */

public class ResponseActivity extends AppCompatActivity{
    // Views
    private TextView cardTokenTextView, cardLastFourTextView, responseTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response);

        Bundle bundle = getIntent().getExtras();

        cardTokenTextView = (TextView) findViewById(R.id.textViewToken);
        cardLastFourTextView = (TextView) findViewById(R.id.textViewCardLastFour);
        responseTextView = (TextView) findViewById(R.id.textViewAPIResponse);

        cardTokenTextView.setText(bundle.getString("token"));
        cardLastFourTextView.setText(bundle.getString("cardLastFour"));
        responseTextView.setText(bundle.getString("response"));
    }
}
