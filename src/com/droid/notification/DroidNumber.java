package com.droid.notification;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import android.widget.NumberPicker;

public class DroidNumber extends DialogPreference {
    int seconds = 10;
	protected NumberPicker picker=null;
	protected TextView timeDisplay;

	public DroidNumber(Context ctxt) {
		this(ctxt, null);
	}

	public DroidNumber(Context ctxt, AttributeSet attrs) {
		this(ctxt, attrs, 0);
	}

	public DroidNumber(Context ctxt, AttributeSet attrs, int defStyle) {
		super(ctxt, attrs, defStyle);
		setPositiveButtonText(R.string.set);
		setNegativeButtonText(R.string.cancel);

        if (attrs == null) {
            return;
        }
    }

	@Override
	protected View onCreateDialogView() {
		picker=new NumberPicker(getContext().getApplicationContext());

        if (this.getKey().contains("timeNotification"))
        {
            int sdk_int = android.os.Build.VERSION.SDK_INT;
            if (sdk_int >=17)
            {
                picker.setMinValue(10);
                picker.setMaxValue(30);
            }
            else
            {
                picker.setMinValue(2);
                picker.setMaxValue(30);
            }
        }
		return(picker);
	}

	@Override
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);
		picker.setValue(seconds);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if (positiveResult) {
			picker.clearFocus();
			seconds=picker.getValue();
			String timeSeconds=String.valueOf(seconds);
   		    persistString(timeSeconds);
		}
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return(a.getString(index));
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		String time=null;

		if (restoreValue) {
			if (defaultValue==null) {
				time=getPersistedString("0");
			}
			else {
				time=getPersistedString(defaultValue.toString());
			}
		}
		else {
			if (defaultValue==null) {
				time="0";
			}
			else {
				time=defaultValue.toString();
			}
			if (shouldPersist()) {
				persistString(time);
			}
		}
		seconds=Integer.parseInt(time);
	}
}