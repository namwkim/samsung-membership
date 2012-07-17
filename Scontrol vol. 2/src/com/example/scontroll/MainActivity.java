package com.example.scontroll;

import android.annotation.SuppressLint;
import android.app.*;
import android.os.Bundle;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.util.Log;
import android.view.View.OnClickListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VerticalSeekBar;
/*import android.widget.VerticalSeekBar_Reverse;*/
import android.os.*;
import android.view.*;
import android.view.View.OnClickListener;

public class MainActivity extends Activity implements OnClickListener {

	SeekBar seekBar, seekBar_coolant, seekBar_battery, seekBar_fuel;
	TextView txtVolume, txtVolume_coolant, txtVolume_battery, txtVolume_fuel;
	// 주용이가 한부분
	private Button neutral, reverse, driving, pivot;
	private Button emergency, parking, monned, unmonned;
	private TextView text; // 요까지]
	private byte emergency_select= 0x00;
	private byte operation_select = 0x01;
	private byte driving_select = 0x01;
	
	// 정호형 한부분
	TextView speed;// 요까지
	
	VerticalSeekBar verticalSeekBar = null;
	/* VerticalSeekBar_Reverse verticalSeekBar_Reverse=null; */
	TextView vsProgress, vs_reverseProgress = null;
	
	//주행제어명령 전송 쓰레드
	private Handler mHandler;
	private NumberThread mNumberThread;


	public String SERVER_IP = "210.118.75.174";
	public int SERVER_PORT = 30001;
	String name;

	Socket socket = null;
	DataInputStream input;
	DataOutputStream output;
	boolean lbCheck = false;

	
	
	

	@SuppressLint("ParserError")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		
		mHandler = new Handler();
		mNumberThread = new NumberThread(true);
		mNumberThread.start();
		
		
		
		//정호형 부분
	    int a=100;
	    speed =(TextView)findViewById(R.id.speedtext);
	    speed.setText(a+""); // 여기까지		
		
		verticalSeekBar = (VerticalSeekBar) findViewById(R.id.vertical_Seekbar);
		/*
		 * verticalSeekBar_Reverse=(VerticalSeekBar_Reverse)findViewById(R.id.
		 * seekbar_reverse);
		 */
		vsProgress = (TextView) findViewById(R.id.vertical_sb_progresstext);
		/*
		 * vs_reverseProgress=(TextView)findViewById(R.id.reverse_sb_progresstext
		 * );
		 */

		// 주용
		neutral = (Button) findViewById(R.id.neutral);
		reverse = (Button) findViewById(R.id.reverse);
		driving = (Button) findViewById(R.id.driving);
		pivot = (Button) findViewById(R.id.pivot);
		emergency = (Button) findViewById(R.id.emergency);
		parking = (Button) findViewById(R.id.parking);
		monned = (Button) findViewById(R.id.monned);
		unmonned = (Button) findViewById(R.id.unmonned);
		// 요기까지

		verticalSeekBar
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					public void onStopTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub
					}

					public void onStartTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub
					}

					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						vsProgress.setText(progress + "");
					}
				});

		/*
		 * verticalSeekBar_Reverse.setOnSeekBarChangeListener(new
		 * OnSeekBarChangeListener() {
		 * 
		 * public void onStopTrackingTouch(SeekBar seekBar) { // TODO
		 * Auto-generated method stub }
		 * 
		 * public void onStartTrackingTouch(SeekBar seekBar) { // TODO
		 * Auto-generated method stub }
		 * 
		 * public void onProgressChanged(SeekBar seekBar, int progress, boolean
		 * fromUser) { vs_reverseProgress.setText(progress+""); } });
		 */

		seekBar = (SeekBar) findViewById(R.id.seekbar);
		txtVolume = (TextView) findViewById(R.id.volume);

		// 시크바의 값이 변경될 때의 이벤트 처리
		seekBar.
		    setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			// 트래킹 종료 시크바를 놓았을때
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			// 트래킹 시작 시크바를 잡았을때
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			// 시크바의 값이 변경될때
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				txtVolume.setText("" + progress);
			}
		});

		seekBar_coolant = (SeekBar) findViewById(R.id.seekbar_coolant);
		seekBar_coolant.setEnabled(false);
		txtVolume_coolant = (TextView) findViewById(R.id.volume_coolant);

		// 시크바의 값이 변경될 때의 이벤트 처리
		seekBar_coolant
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					// 트래킹 종료 시크바를 놓았을때
					public void onStopTrackingTouch(SeekBar seekBar_coolant) {
					}

					// 트래킹 시작 시크바를 잡았을때
					public void onStartTrackingTouch(SeekBar seekBar_coolant) {
					}

					// 시크바의 값이 변경될때
					public void onProgressChanged(SeekBar seekBar_coolant,
							int progress, boolean fromUser) {
						txtVolume_coolant.setText("Coolant : " + progress);
					}
				});

		seekBar_battery = (SeekBar) findViewById(R.id.seekbar_battery);
		seekBar_battery.setEnabled(false);		
		txtVolume_battery = (TextView) findViewById(R.id.volume_battery);

		// 시크바의 값이 변경될 때의 이벤트 처리
		seekBar_battery
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					// 트래킹 종료 시크바를 놓았을때
					public void onStopTrackingTouch(SeekBar seekBar_battery) {
					}

					// 트래킹 시작 시크바를 잡았을때
					public void onStartTrackingTouch(SeekBar seekBar_battery) {
					}

					// 시크바의 값이 변경될때
					public void onProgressChanged(SeekBar seekBar_battery,
							int progress, boolean fromUser) {
						txtVolume_battery.setText("Battery  : " + progress);
					}
				});

		seekBar_fuel = (SeekBar) findViewById(R.id.seekbar_fuel);
		seekBar_fuel.setEnabled(false);
		txtVolume_fuel = (TextView) findViewById(R.id.volume_fuel);

		// 시크바의 값이 변경될 때의 이벤트 처리
		seekBar_fuel.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			// 트래킹 종료 시크바를 놓았을때
			public void onStopTrackingTouch(SeekBar seekBar_fuel) {
			}

			// 트래킹 시작 시크바를 잡았을때
			public void onStartTrackingTouch(SeekBar seekBar_fuel) {
			}

			// 시크바의 값이 변경될때
			public void onProgressChanged(SeekBar seekBar_fuel, int progress,
					boolean fromUser) {
				txtVolume_fuel.setText("F u e l　: " + progress);
			}
		});
		//주용이가 한부분
		neutral.setOnClickListener(this);
		reverse.setOnClickListener(this);
		driving.setOnClickListener(this);
		pivot.setOnClickListener(this);
		emergency.setOnClickListener(this);
		parking.setOnClickListener(this);
		monned.setOnClickListener(this);
		unmonned.setOnClickListener(this);
        //요기까지
	}
    //주용이가 한부분
	public void onClick(View v) {

		byte flag;
		byte data;
		switch (v.getId()) {


			
		case R.id.emergency:
			if(emergency_select == 0)
			{
				emergency.setBackgroundColor(0xffff0000);
				emergency_select = 1;
				flag = 0x11;
				  data = (byte) 0xff;
				 try {							
					 output.write(flag);							
					 output.write(data);							
				} catch (IOException e) {
					
					e.printStackTrace();
				}
			}
			else if(emergency_select == 1)
			{
				emergency.setBackgroundColor(0xff50c8FF);
				emergency_select = 0;
				flag = 0x11;
				  data = 0x00;
				 try {							
					 output.write(flag);							
					 output.write(data);							
				} catch (IOException e) {
					
					e.printStackTrace();
				}
			}
			
			else if(emergency_select == 0xFF)
			{
				emergency.setBackgroundColor(0xff50c8FF);
				emergency_select = 0x00;
				 //비상정지 명령 전송
				 flag = 0x11;
				  data = (byte) emergency_select;
				 try {							
					 output.write(flag);							
					 output.write(data);							
				} catch (IOException e) {
					
					e.printStackTrace();
				}
			}
			
			break;

			//기동파킹
		case R.id.parking:
			parking.setBackgroundColor(0xff0064ff);
			monned.setBackgroundColor(0xff50c8FF);
			unmonned.setBackgroundColor(0xff50c8FF);
			operation_select = 0x01;
			 //기동모드 명령 전송
			 flag = 0x21;
			 data = (byte) operation_select;
			 try {							
				 output.write(flag);							
				 output.write(data);							
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			break;

			//유인기동
		case R.id.monned:
			parking.setBackgroundColor(0xff50c8FF);
			monned.setBackgroundColor(0xff0064ff);
			unmonned.setBackgroundColor(0xff50c8FF);
			operation_select = 0x02;
			 //기동모드 명령 전송
			 flag = 0x21;
			 data = (byte) operation_select;
			 try {							
				 output.write(flag);							
				 output.write(data);							
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			break;

			//무인기동
		case R.id.unmonned:
			parking.setBackgroundColor(0xff50c8FF);
			monned.setBackgroundColor(0xff50c8FF);
			unmonned.setBackgroundColor(0xff0064ff);
			operation_select = 0x03;
			 //기동모드 명령 전송
			 flag = 0x21;
			 data = (byte) operation_select;
			 try {							
				 output.write(flag);							
				 output.write(data);							
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			break;

			//중립기어
		case R.id.neutral:
			neutral.setBackgroundColor(0xff288C28);
			reverse.setBackgroundColor(0xff66CDAA);
			driving.setBackgroundColor(0xff66CDAA);
			pivot.setBackgroundColor(0xff66CDAA);
			driving_select = 0x01;
			 //주행모드 명령 전송
			 flag = 0x31;
			 data = (byte) driving_select;
			 try {							
				 output.write(flag);							
				 output.write(data);							
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			break;

			
			//후진기어
		case R.id.reverse:
			neutral.setBackgroundColor(0xff66CDAA);
			reverse.setBackgroundColor(0xff288C28);
			driving.setBackgroundColor(0xff66CDAA);
			pivot.setBackgroundColor(0xff66CDAA);
			driving_select = 0x02;
			 //주행모드 명령 전송
			 flag = 0x31;
			 data = (byte) driving_select;
			 try {							
				 output.write(flag);							
				 output.write(data);							
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			break;

			//전진기어
		case R.id.driving:
			neutral.setBackgroundColor(0xff66CDAA);
			reverse.setBackgroundColor(0xff66CDAA);
			driving.setBackgroundColor(0xff288C28);
			pivot.setBackgroundColor(0xff66CDAA);
			driving_select = 0x03;
			 //주행모드 명령 전송
			 flag = 0x31;
			 data = (byte) driving_select;
			 try {							
				 output.write(flag);							
				 output.write(data);							
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			break;

			
			//선회
		case R.id.pivot:
			neutral.setBackgroundColor(0xff66CDAA);
			reverse.setBackgroundColor(0xff66CDAA);
			driving.setBackgroundColor(0xff66CDAA);
			pivot.setBackgroundColor(0xff288C28);
			driving_select = 0x04;
			 //주행모드 명령 전송
			 flag = 0x31;
			 data = (byte) driving_select;
			 try {							
				 output.write(flag);							
				 output.write(data);							
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			break;

		default:
			break;

		}

	}
	//요기까지
	
	class NumberThread extends Thread {

		
		private int i = 0;
		private boolean isPlay = false;

		public void connect() {
			try {
				Log.d("Thead", "connect1" + socket);
				socket = new Socket(SERVER_IP, SERVER_PORT);
				Log.d("Thead", "connect2" + socket);
				input = new DataInputStream(socket.getInputStream());
				output = new DataOutputStream(socket.getOutputStream());

				while (socket != null) {
					if (socket.isConnected()) {
						output.flush();
						break;
					}
				}

			} catch (Exception e) {
				// this.finish();
			}
		}

		public NumberThread(boolean isPlay) {
			this.isPlay = isPlay;
		}

		public void stopThread() {
			isPlay = !isPlay;
		}

		//송신한 6칸의 배열의 바이트 오더를 맞추어준다.
		 public void swap(byte buf[]){
			 byte tmp;
			 tmp = buf[0];
			 buf[0] = buf[1];
			 buf[1] = tmp;
			 
			 tmp = buf[2];
			 buf[2] = buf[3];
			 buf[3] = tmp;
			 
			 tmp = buf[4];
			 buf[4] = buf[5];
			 buf[5] = tmp;
		 }
		
		@Override
		public void run() {
			super.run();
			connect();
			while (isPlay) {
				
				try {
					Thread.sleep(1000);
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
						
					
				mHandler.post(new Runnable() {
					
					public void run() {
						//주행제어 명령 전송
						short STR_CMD = (short)seekBar.getProgress(); // 현재 방향값을 수신
						short accel = (short)verticalSeekBar.getProgress();
						short DRV_CMD = 0;
						short BRK_CMD = 0;
						if(accel>100)
						{
							DRV_CMD = (short) ((accel -100)); 
						}
						else
						{
							BRK_CMD = (short) ((accel -100)*(-1));
						}
						byte flag = 0x46;
						ByteBuffer buff1 = ByteBuffer.allocate(6);
						buff1.putShort(DRV_CMD);
						buff1.putShort(STR_CMD);
						buff1.putShort(BRK_CMD);
						byte buf1 [] = buff1.array();
						swap(buf1);
						 try {
							 Log.d("Thead", "send1"+DRV_CMD);
							 output.write(flag);
							 Log.d("Thead", "send2"+STR_CMD);
							 output.write(buf1);
							 Log.d("Thead", "send3"+BRK_CMD);
						} catch (IOException e) {
							
							e.printStackTrace();
						}
						 
						 
						 /*
						 //비상정지 명령 전송
						 flag = 0x11;
						 byte data = (byte) emergency_select;
						 try {							
							 output.write(flag);							
							 output.write(data);							
						} catch (IOException e) {
							
							e.printStackTrace();
						}
						 
						 
						 try {
								Thread.sleep(100);
								
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						 
						 //기동모드 명령 전송
						 flag = 0x21;
						 data = (byte) operation_select;
						 try {							
							 output.write(flag);							
							 output.write(data);							
						} catch (IOException e) {
							
							e.printStackTrace();
						}
						 
						 try {
								Thread.sleep(100);
								
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						 
						 
						 //주행모드 명령 전송
						 flag = 0x31;
						 data = (byte) driving_select;
						 try {							
							 output.write(flag);							
							 output.write(data);							
						} catch (IOException e) {
							
							e.printStackTrace();
						}
						 */
						 
						 
					}
				});
				
				
			}
		}
	}

	
}

