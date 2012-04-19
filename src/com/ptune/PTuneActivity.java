package com.ptune;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ptune.audio.CaptureTask;
import com.ptune.audio.CaptureThread;
import com.ptune.graphics.DialSurfaceView;
import com.ptune.graphics.DialView;

public class PTuneActivity extends Activity {
	private DialView dial;
	private TextView t;
	//private CaptureTask capture;
	private float targetFrequency;
	private CaptureThread mCapture;
	private Handler mHandler;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        dial = (DialView) findViewById(R.id.dial);
        t = (TextView) findViewById(R.id.textView1);

        updateTargetFrequency(); // Get radio button selection
        
        mHandler = new Handler() {
        	@Override
        	public void handleMessage(Message m) {
        		updateDisplay(m.getData().getFloat("Freq"));
        	}
        };
        
        mCapture = new CaptureThread(mHandler);
        mCapture.setRunning(true);
        mCapture.start();
        
        //Log.d("PTuneActivity", "onCreate called.");
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	
    	if (mCapture != null) {
    		mCapture.setRunning(false);
    		mCapture = null;
    	}
    	
    	//Log.d("PTuneActivity", "onDestroy called.");
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	
    	mCapture.setRunning(false);
    	
    	//Log.d("PTuneActivity", "onPause called.");
    }
    
    @Override
    protected void onResume() {
    	super.onResume();  

    	updateTargetFrequency(); // Get radio button selection
        
        //Log.d("PTuneActivity", "onResume called.");
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	RadioButton rb;
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.b_std:
            	rb = (RadioButton) findViewById(R.id.radio0);
            	rb.setText("B3");
            	rb.setTag("246.942");
            	rb = (RadioButton) findViewById(R.id.radio1);
            	rb.setText("G3");
            	rb.setTag("195.998");
            	rb = (RadioButton) findViewById(R.id.radio2);
            	rb.setText("D3");
            	rb.setTag("146.832");
            	rb = (RadioButton) findViewById(R.id.radio3);
            	rb.setText("A2");
            	rb.setTag("110.000");
            	rb = (RadioButton) findViewById(R.id.radio4);
            	rb.setText("E2");
            	rb.setTag("82.4069");
            	rb = (RadioButton) findViewById(R.id.radio5);
            	rb.setText("B1");
            	rb.setTag("61.7354");
            	updateTargetFrequency();
            	Toast.makeText(PTuneActivity.this, R.string.b_std, Toast.LENGTH_SHORT).show();
                return true;
            case R.id.e_std:
            	rb = (RadioButton) findViewById(R.id.radio0);
            	rb.setText("E4");
            	rb.setTag("329.628");
            	rb = (RadioButton) findViewById(R.id.radio1);
            	rb.setText("B3");
            	rb.setTag("246.942");
            	rb = (RadioButton) findViewById(R.id.radio2);
            	rb.setText("G3");
            	rb.setTag("195.998");
            	rb = (RadioButton) findViewById(R.id.radio3);
            	rb.setText("D3");
            	rb.setTag("146.832");
            	rb = (RadioButton) findViewById(R.id.radio4);
            	rb.setText("A2");
            	rb.setTag("110.000");
            	rb = (RadioButton) findViewById(R.id.radio5);
            	rb.setText("E2");
            	rb.setTag("82.4069");
            	updateTargetFrequency();
            	Toast.makeText(PTuneActivity.this, R.string.e_std, Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private void updateTargetFrequency() {
    	// Grab the selected radio button tag.
        RadioGroup rg = (RadioGroup) findViewById(R.id.radioGroup1);
        int selected = rg.getCheckedRadioButtonId();
        RadioButton rb = (RadioButton) findViewById(selected);
        targetFrequency = Float.parseFloat((String)rb.getTag());
    }
    
    public void updateDisplay(float frequency) {
    	// Calculate difference between target and measured frequency,
    	// given that the measured frequency can be a factor of target.
    	float difference = 0;
    	if (frequency > targetFrequency) {
    		int divisions = (int) (frequency / targetFrequency);
    		float modified = targetFrequency * (float) divisions;
    		if (frequency - modified > targetFrequency / 2) {
    			modified += targetFrequency;
    			divisions++;
    		}
    		difference = (frequency - modified) / (float) divisions;
    	} else {
    		// If target is greater than measured, just use difference.
    		difference = frequency - targetFrequency;
    	}
    	
    	float relativeFrequency = targetFrequency + difference;
    	
    	// Update TextView
    	if (relativeFrequency < 1000f)
			t.setText(String.format("%.1f Hz", relativeFrequency));
		else
			t.setText(String.format("%.2f kHz", relativeFrequency/1000));

    	// Update DialView
    	float value = difference / (targetFrequency / 2) * 90;
		dial.update(value);
    }
    
    public void onRadioButtonClicked(View v) {
    	// Perform action on clicks
    	RadioButton rb = (RadioButton) v;
    	Toast.makeText(PTuneActivity.this, rb.getText(), Toast.LENGTH_SHORT).show();
    	targetFrequency = Float.parseFloat((String)rb.getTag());
    }
}