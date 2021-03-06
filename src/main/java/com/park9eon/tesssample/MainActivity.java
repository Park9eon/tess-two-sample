package com.park9eon.tesssample;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.*;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

	private Button galleryButton;
	private TextView resultMessage;
	private Spinner spinner;
	private TessBaseAPI baseAPI;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.galleryButton = (Button) findViewById(R.id.galleryButton);
		this.resultMessage = (TextView) findViewById(R.id.resultMessage);
		this.spinner = (Spinner) findViewById(R.id.langSpinner);
		this.spinner.setOnItemSelectedListener(this);

		this.baseAPI = new TessBaseAPI();
		this.baseAPI.setDebug(true);

		copyFiles();

		setLang(0);
		baseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SPARSE_TEXT);
	}

	// Stackoverflow에서 퍼옴
	private void copyFiles() {
		AssetManager assetManager = getAssets();
		String[] files = {"eng.traineddata", "kor.traineddata"};
		File dataDir = new File(getTraineddataDir() + "/tessdata");
		if (files != null) for (String filename : files) {
			InputStream in = null;
			OutputStream out = null;
			try {
				in = assetManager.open(filename);
				File outFile = new File(dataDir, filename);
				out = new FileOutputStream(outFile);
				copyFile(in, out);
			} catch(IOException e) {
				Log.e("tag", "Failed to copy asset file: " + filename, e);
			}
			finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						// NOOP
					}
				}
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						// NOOP
					}
				}
			}
		}
	}
	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while((read = in.read(buffer)) != -1){
			out.write(buffer, 0, read);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case 0:
				if (resultCode == RESULT_OK) {
					inspect(data.getData());
				}
				break;
			default :
				super.onActivityResult(requestCode, resultCode, data);
				break;
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		setLang(position);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	private String getTraineddataDir() {
		String path = Environment.getExternalStorageDirectory().getPath() + "/data/tesseract/";
		File file = new File(path + "/tessdata");
		if (!file.exists()) {
			file.mkdirs();
		}
		return path;
	}

	public void setLang(int index) {
		String[] langs = getResources().getStringArray(R.array.langs);
		if (langs.length > index && index >= 0) {
			String path = getTraineddataDir();
			try {
				baseAPI.init(path, langs[index]);
			} catch (Exception e) {
				this.resultMessage.setText(path + "||" + e.toString());
			}
		}
	}

	@Override
	public void onClick(View v) {
		// xml 세팅 : 버튼클릭시 갤린더를 열어줌
		if (v.getId() == R.id.galleryButton) {
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(intent, 0);
		}
	}

	private void inspect(Uri uri) {
		InputStream is = null;
		try {
			is = getContentResolver().openInputStream(uri);
			Bitmap bitmap = BitmapFactory.decodeStream(is);
			inspectFromBitmap(bitmap);
		} catch (FileNotFoundException e) {
			this.resultMessage.setText("[" + e.getMessage() + "]" + e.toString());
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					this.resultMessage.setText("[" + e.getMessage() + "]" + e.toString());
				}
			}
		}
	}

	private void inspectFromBitmap(Bitmap bitmap) {
		baseAPI.setImage(bitmap);
		String text = baseAPI.getUTF8Text();
		this.resultMessage.setText(text);
		bitmap = null;
	}

}
