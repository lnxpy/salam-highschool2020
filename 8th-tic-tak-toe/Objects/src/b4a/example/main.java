package b4a.example;


import anywheresoftware.b4a.B4AMenuItem;
import android.app.Activity;
import android.os.Bundle;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.B4AActivity;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.objects.ActivityWrapper;
import java.lang.reflect.InvocationTargetException;
import anywheresoftware.b4a.B4AUncaughtException;
import anywheresoftware.b4a.debug.*;
import java.lang.ref.WeakReference;

public class main extends Activity implements B4AActivity{
	public static main mostCurrent;
	static boolean afterFirstLayout;
	static boolean isFirst = true;
    private static boolean processGlobalsRun = false;
	BALayout layout;
	public static BA processBA;
	BA activityBA;
    ActivityWrapper _activity;
    java.util.ArrayList<B4AMenuItem> menuItems;
	public static final boolean fullScreen = false;
	public static final boolean includeTitle = true;
    public static WeakReference<Activity> previousOne;
    public static boolean dontPause;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        mostCurrent = this;
		if (processBA == null) {
			processBA = new BA(this.getApplicationContext(), null, null, "b4a.example", "b4a.example.main");
			processBA.loadHtSubs(this.getClass());
	        float deviceScale = getApplicationContext().getResources().getDisplayMetrics().density;
	        BALayout.setDeviceScale(deviceScale);
            
		}
		else if (previousOne != null) {
			Activity p = previousOne.get();
			if (p != null && p != this) {
                BA.LogInfo("Killing previous instance (main).");
				p.finish();
			}
		}
        processBA.setActivityPaused(true);
        processBA.runHook("oncreate", this, null);
		if (!includeTitle) {
        	this.getWindow().requestFeature(android.view.Window.FEATURE_NO_TITLE);
        }
        if (fullScreen) {
        	getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,   
        			android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
		
        processBA.sharedProcessBA.activityBA = null;
		layout = new BALayout(this);
		setContentView(layout);
		afterFirstLayout = false;
        WaitForLayout wl = new WaitForLayout();
        if (anywheresoftware.b4a.objects.ServiceHelper.StarterHelper.startFromActivity(this, processBA, wl, true))
		    BA.handler.postDelayed(wl, 5);

	}
	static class WaitForLayout implements Runnable {
		public void run() {
			if (afterFirstLayout)
				return;
			if (mostCurrent == null)
				return;
            
			if (mostCurrent.layout.getWidth() == 0) {
				BA.handler.postDelayed(this, 5);
				return;
			}
			mostCurrent.layout.getLayoutParams().height = mostCurrent.layout.getHeight();
			mostCurrent.layout.getLayoutParams().width = mostCurrent.layout.getWidth();
			afterFirstLayout = true;
			mostCurrent.afterFirstLayout();
		}
	}
	private void afterFirstLayout() {
        if (this != mostCurrent)
			return;
		activityBA = new BA(this, layout, processBA, "b4a.example", "b4a.example.main");
        
        processBA.sharedProcessBA.activityBA = new java.lang.ref.WeakReference<BA>(activityBA);
        anywheresoftware.b4a.objects.ViewWrapper.lastId = 0;
        _activity = new ActivityWrapper(activityBA, "activity");
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (BA.isShellModeRuntimeCheck(processBA)) {
			if (isFirst)
				processBA.raiseEvent2(null, true, "SHELL", false);
			processBA.raiseEvent2(null, true, "CREATE", true, "b4a.example.main", processBA, activityBA, _activity, anywheresoftware.b4a.keywords.Common.Density, mostCurrent);
			_activity.reinitializeForShell(activityBA, "activity");
		}
        initializeProcessGlobals();		
        initializeGlobals();
        
        BA.LogInfo("** Activity (main) Create, isFirst = " + isFirst + " **");
        processBA.raiseEvent2(null, true, "activity_create", false, isFirst);
		isFirst = false;
		if (this != mostCurrent)
			return;
        processBA.setActivityPaused(false);
        BA.LogInfo("** Activity (main) Resume **");
        processBA.raiseEvent(null, "activity_resume");
        if (android.os.Build.VERSION.SDK_INT >= 11) {
			try {
				android.app.Activity.class.getMethod("invalidateOptionsMenu").invoke(this,(Object[]) null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	public void addMenuItem(B4AMenuItem item) {
		if (menuItems == null)
			menuItems = new java.util.ArrayList<B4AMenuItem>();
		menuItems.add(item);
	}
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
        try {
            if (processBA.subExists("activity_actionbarhomeclick")) {
                Class.forName("android.app.ActionBar").getMethod("setHomeButtonEnabled", boolean.class).invoke(
                    getClass().getMethod("getActionBar").invoke(this), true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (processBA.runHook("oncreateoptionsmenu", this, new Object[] {menu}))
            return true;
		if (menuItems == null)
			return false;
		for (B4AMenuItem bmi : menuItems) {
			android.view.MenuItem mi = menu.add(bmi.title);
			if (bmi.drawable != null)
				mi.setIcon(bmi.drawable);
            if (android.os.Build.VERSION.SDK_INT >= 11) {
				try {
                    if (bmi.addToBar) {
				        android.view.MenuItem.class.getMethod("setShowAsAction", int.class).invoke(mi, 1);
                    }
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			mi.setOnMenuItemClickListener(new B4AMenuItemsClickListener(bmi.eventName.toLowerCase(BA.cul)));
		}
        
		return true;
	}   
 @Override
 public boolean onOptionsItemSelected(android.view.MenuItem item) {
    if (item.getItemId() == 16908332) {
        processBA.raiseEvent(null, "activity_actionbarhomeclick");
        return true;
    }
    else
        return super.onOptionsItemSelected(item); 
}
@Override
 public boolean onPrepareOptionsMenu(android.view.Menu menu) {
    super.onPrepareOptionsMenu(menu);
    processBA.runHook("onprepareoptionsmenu", this, new Object[] {menu});
    return true;
    
 }
 protected void onStart() {
    super.onStart();
    processBA.runHook("onstart", this, null);
}
 protected void onStop() {
    super.onStop();
    processBA.runHook("onstop", this, null);
}
    public void onWindowFocusChanged(boolean hasFocus) {
       super.onWindowFocusChanged(hasFocus);
       if (processBA.subExists("activity_windowfocuschanged"))
           processBA.raiseEvent2(null, true, "activity_windowfocuschanged", false, hasFocus);
    }
	private class B4AMenuItemsClickListener implements android.view.MenuItem.OnMenuItemClickListener {
		private final String eventName;
		public B4AMenuItemsClickListener(String eventName) {
			this.eventName = eventName;
		}
		public boolean onMenuItemClick(android.view.MenuItem item) {
			processBA.raiseEventFromUI(item.getTitle(), eventName + "_click");
			return true;
		}
	}
    public static Class<?> getObject() {
		return main.class;
	}
    private Boolean onKeySubExist = null;
    private Boolean onKeyUpSubExist = null;
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (processBA.runHook("onkeydown", this, new Object[] {keyCode, event}))
            return true;
		if (onKeySubExist == null)
			onKeySubExist = processBA.subExists("activity_keypress");
		if (onKeySubExist) {
			if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK &&
					android.os.Build.VERSION.SDK_INT >= 18) {
				HandleKeyDelayed hk = new HandleKeyDelayed();
				hk.kc = keyCode;
				BA.handler.post(hk);
				return true;
			}
			else {
				boolean res = new HandleKeyDelayed().runDirectly(keyCode);
				if (res)
					return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	private class HandleKeyDelayed implements Runnable {
		int kc;
		public void run() {
			runDirectly(kc);
		}
		public boolean runDirectly(int keyCode) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keypress", false, keyCode);
			if (res == null || res == true) {
                return true;
            }
            else if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK) {
				finish();
				return true;
			}
            return false;
		}
		
	}
    @Override
	public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
        if (processBA.runHook("onkeyup", this, new Object[] {keyCode, event}))
            return true;
		if (onKeyUpSubExist == null)
			onKeyUpSubExist = processBA.subExists("activity_keyup");
		if (onKeyUpSubExist) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keyup", false, keyCode);
			if (res == null || res == true)
				return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	@Override
	public void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
		this.setIntent(intent);
        processBA.runHook("onnewintent", this, new Object[] {intent});
	}
    @Override 
	public void onPause() {
		super.onPause();
        if (_activity == null)
            return;
        if (this != mostCurrent)
			return;
		anywheresoftware.b4a.Msgbox.dismiss(true);
        if (!dontPause)
            BA.LogInfo("** Activity (main) Pause, UserClosed = " + activityBA.activity.isFinishing() + " **");
        else
            BA.LogInfo("** Activity (main) Pause event (activity is not paused). **");
        if (mostCurrent != null)
            processBA.raiseEvent2(_activity, true, "activity_pause", false, activityBA.activity.isFinishing());		
        if (!dontPause) {
            processBA.setActivityPaused(true);
            mostCurrent = null;
        }

        if (!activityBA.activity.isFinishing())
			previousOne = new WeakReference<Activity>(this);
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        processBA.runHook("onpause", this, null);
	}

	@Override
	public void onDestroy() {
        super.onDestroy();
		previousOne = null;
        processBA.runHook("ondestroy", this, null);
	}
    @Override 
	public void onResume() {
		super.onResume();
        mostCurrent = this;
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (activityBA != null) { //will be null during activity create (which waits for AfterLayout).
        	ResumeMessage rm = new ResumeMessage(mostCurrent);
        	BA.handler.post(rm);
        }
        processBA.runHook("onresume", this, null);
	}
    private static class ResumeMessage implements Runnable {
    	private final WeakReference<Activity> activity;
    	public ResumeMessage(Activity activity) {
    		this.activity = new WeakReference<Activity>(activity);
    	}
		public void run() {
            main mc = mostCurrent;
			if (mc == null || mc != activity.get())
				return;
			processBA.setActivityPaused(false);
            BA.LogInfo("** Activity (main) Resume **");
            if (mc != mostCurrent)
                return;
		    processBA.raiseEvent(mc._activity, "activity_resume", (Object[])null);
		}
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	      android.content.Intent data) {
		processBA.onActivityResult(requestCode, resultCode, data);
        processBA.runHook("onactivityresult", this, new Object[] {requestCode, resultCode});
	}
	private static void initializeGlobals() {
		processBA.raiseEvent2(null, true, "globals", false, (Object[])null);
	}
    public void onRequestPermissionsResult(int requestCode,
        String permissions[], int[] grantResults) {
        for (int i = 0;i < permissions.length;i++) {
            Object[] o = new Object[] {permissions[i], grantResults[i] == 0};
            processBA.raiseEventFromDifferentThread(null,null, 0, "activity_permissionresult", true, o);
        }
            
    }

public anywheresoftware.b4a.keywords.Common __c = null;
public static boolean _turn = false;
public anywheresoftware.b4a.objects.LabelWrapper _lbl_p1point = null;
public anywheresoftware.b4a.objects.LabelWrapper _lbl_p2point = null;
public anywheresoftware.b4a.objects.ButtonWrapper _btn_cell1 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _btn_cell2 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _btn_cell3 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _btn_cell4 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _btn_cell5 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _btn_cell6 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _btn_cell7 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _btn_cell8 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _btn_cell9 = null;

public static boolean isAnyActivityVisible() {
    boolean vis = false;
vis = vis | (main.mostCurrent != null);
return vis;}
public static String  _activity_create(boolean _firsttime) throws Exception{
 //BA.debugLineNum = 40;BA.debugLine="Sub Activity_Create(FirstTime As Boolean)";
 //BA.debugLineNum = 42;BA.debugLine="Activity.LoadLayout(\"Layout1\")";
mostCurrent._activity.LoadLayout("Layout1",mostCurrent.activityBA);
 //BA.debugLineNum = 44;BA.debugLine="End Sub";
return "";
}
public static String  _activity_pause(boolean _userclosed) throws Exception{
 //BA.debugLineNum = 50;BA.debugLine="Sub Activity_Pause (UserClosed As Boolean)";
 //BA.debugLineNum = 52;BA.debugLine="End Sub";
return "";
}
public static String  _activity_resume() throws Exception{
 //BA.debugLineNum = 46;BA.debugLine="Sub Activity_Resume";
 //BA.debugLineNum = 48;BA.debugLine="End Sub";
return "";
}
public static String  _btn_cell1_click() throws Exception{
 //BA.debugLineNum = 158;BA.debugLine="Sub btn_cell1_Click";
 //BA.debugLineNum = 159;BA.debugLine="If turn Then";
if (_turn) { 
 //BA.debugLineNum = 160;BA.debugLine="btn_cell1.Text = \"X\"";
mostCurrent._btn_cell1.setText(BA.ObjectToCharSequence("X"));
 //BA.debugLineNum = 161;BA.debugLine="turn = False";
_turn = anywheresoftware.b4a.keywords.Common.False;
 }else {
 //BA.debugLineNum = 163;BA.debugLine="btn_cell1.Text = \"O\"";
mostCurrent._btn_cell1.setText(BA.ObjectToCharSequence("O"));
 //BA.debugLineNum = 164;BA.debugLine="turn = True";
_turn = anywheresoftware.b4a.keywords.Common.True;
 };
 //BA.debugLineNum = 166;BA.debugLine="End Sub";
return "";
}
public static String  _btn_cell2_click() throws Exception{
 //BA.debugLineNum = 148;BA.debugLine="Sub btn_cell2_Click";
 //BA.debugLineNum = 149;BA.debugLine="If turn Then";
if (_turn) { 
 //BA.debugLineNum = 150;BA.debugLine="btn_cell2.Text = \"X\"";
mostCurrent._btn_cell2.setText(BA.ObjectToCharSequence("X"));
 //BA.debugLineNum = 151;BA.debugLine="turn = False";
_turn = anywheresoftware.b4a.keywords.Common.False;
 }else {
 //BA.debugLineNum = 153;BA.debugLine="btn_cell2.Text = \"O\"";
mostCurrent._btn_cell2.setText(BA.ObjectToCharSequence("O"));
 //BA.debugLineNum = 154;BA.debugLine="turn = True";
_turn = anywheresoftware.b4a.keywords.Common.True;
 };
 //BA.debugLineNum = 156;BA.debugLine="End Sub";
return "";
}
public static String  _btn_cell3_click() throws Exception{
 //BA.debugLineNum = 138;BA.debugLine="Sub btn_cell3_Click";
 //BA.debugLineNum = 139;BA.debugLine="If turn Then";
if (_turn) { 
 //BA.debugLineNum = 140;BA.debugLine="btn_cell3.Text = \"X\"";
mostCurrent._btn_cell3.setText(BA.ObjectToCharSequence("X"));
 //BA.debugLineNum = 141;BA.debugLine="turn = False";
_turn = anywheresoftware.b4a.keywords.Common.False;
 }else {
 //BA.debugLineNum = 143;BA.debugLine="btn_cell3.Text = \"O\"";
mostCurrent._btn_cell3.setText(BA.ObjectToCharSequence("O"));
 //BA.debugLineNum = 144;BA.debugLine="turn = True";
_turn = anywheresoftware.b4a.keywords.Common.True;
 };
 //BA.debugLineNum = 146;BA.debugLine="End Sub";
return "";
}
public static String  _btn_cell4_click() throws Exception{
 //BA.debugLineNum = 128;BA.debugLine="Sub btn_cell4_Click";
 //BA.debugLineNum = 129;BA.debugLine="If turn Then";
if (_turn) { 
 //BA.debugLineNum = 130;BA.debugLine="btn_cell4.Text = \"X\"";
mostCurrent._btn_cell4.setText(BA.ObjectToCharSequence("X"));
 //BA.debugLineNum = 131;BA.debugLine="turn = False";
_turn = anywheresoftware.b4a.keywords.Common.False;
 }else {
 //BA.debugLineNum = 133;BA.debugLine="btn_cell4.Text = \"O\"";
mostCurrent._btn_cell4.setText(BA.ObjectToCharSequence("O"));
 //BA.debugLineNum = 134;BA.debugLine="turn = True";
_turn = anywheresoftware.b4a.keywords.Common.True;
 };
 //BA.debugLineNum = 136;BA.debugLine="End Sub";
return "";
}
public static String  _btn_cell5_click() throws Exception{
 //BA.debugLineNum = 118;BA.debugLine="Sub btn_cell5_Click";
 //BA.debugLineNum = 119;BA.debugLine="If turn Then";
if (_turn) { 
 //BA.debugLineNum = 120;BA.debugLine="btn_cell5.Text = \"X\"";
mostCurrent._btn_cell5.setText(BA.ObjectToCharSequence("X"));
 //BA.debugLineNum = 121;BA.debugLine="turn = False";
_turn = anywheresoftware.b4a.keywords.Common.False;
 }else {
 //BA.debugLineNum = 123;BA.debugLine="btn_cell5.Text = \"O\"";
mostCurrent._btn_cell5.setText(BA.ObjectToCharSequence("O"));
 //BA.debugLineNum = 124;BA.debugLine="turn = True";
_turn = anywheresoftware.b4a.keywords.Common.True;
 };
 //BA.debugLineNum = 126;BA.debugLine="End Sub";
return "";
}
public static String  _btn_cell6_click() throws Exception{
 //BA.debugLineNum = 108;BA.debugLine="Sub btn_cell6_Click";
 //BA.debugLineNum = 109;BA.debugLine="If turn Then";
if (_turn) { 
 //BA.debugLineNum = 110;BA.debugLine="btn_cell6.Text = \"X\"";
mostCurrent._btn_cell6.setText(BA.ObjectToCharSequence("X"));
 //BA.debugLineNum = 111;BA.debugLine="turn = False";
_turn = anywheresoftware.b4a.keywords.Common.False;
 }else {
 //BA.debugLineNum = 113;BA.debugLine="btn_cell6.Text = \"O\"";
mostCurrent._btn_cell6.setText(BA.ObjectToCharSequence("O"));
 //BA.debugLineNum = 114;BA.debugLine="turn = True";
_turn = anywheresoftware.b4a.keywords.Common.True;
 };
 //BA.debugLineNum = 116;BA.debugLine="End Sub";
return "";
}
public static String  _btn_cell7_click() throws Exception{
 //BA.debugLineNum = 98;BA.debugLine="Sub btn_cell7_Click";
 //BA.debugLineNum = 99;BA.debugLine="If turn Then";
if (_turn) { 
 //BA.debugLineNum = 100;BA.debugLine="btn_cell7.Text = \"X\"";
mostCurrent._btn_cell7.setText(BA.ObjectToCharSequence("X"));
 //BA.debugLineNum = 101;BA.debugLine="turn = False";
_turn = anywheresoftware.b4a.keywords.Common.False;
 }else {
 //BA.debugLineNum = 103;BA.debugLine="btn_cell7.Text = \"O\"";
mostCurrent._btn_cell7.setText(BA.ObjectToCharSequence("O"));
 //BA.debugLineNum = 104;BA.debugLine="turn = True";
_turn = anywheresoftware.b4a.keywords.Common.True;
 };
 //BA.debugLineNum = 106;BA.debugLine="End Sub";
return "";
}
public static String  _btn_cell8_click() throws Exception{
 //BA.debugLineNum = 88;BA.debugLine="Sub btn_cell8_Click";
 //BA.debugLineNum = 89;BA.debugLine="If turn Then";
if (_turn) { 
 //BA.debugLineNum = 90;BA.debugLine="btn_cell8.Text = \"X\"";
mostCurrent._btn_cell8.setText(BA.ObjectToCharSequence("X"));
 //BA.debugLineNum = 91;BA.debugLine="turn = False";
_turn = anywheresoftware.b4a.keywords.Common.False;
 }else {
 //BA.debugLineNum = 93;BA.debugLine="btn_cell8.Text = \"O\"";
mostCurrent._btn_cell8.setText(BA.ObjectToCharSequence("O"));
 //BA.debugLineNum = 94;BA.debugLine="turn = True";
_turn = anywheresoftware.b4a.keywords.Common.True;
 };
 //BA.debugLineNum = 96;BA.debugLine="End Sub";
return "";
}
public static String  _btn_cell9_click() throws Exception{
 //BA.debugLineNum = 78;BA.debugLine="Sub btn_cell9_Click";
 //BA.debugLineNum = 79;BA.debugLine="If turn Then";
if (_turn) { 
 //BA.debugLineNum = 80;BA.debugLine="btn_cell9.Text = \"X\"";
mostCurrent._btn_cell9.setText(BA.ObjectToCharSequence("X"));
 //BA.debugLineNum = 81;BA.debugLine="turn = False";
_turn = anywheresoftware.b4a.keywords.Common.False;
 }else {
 //BA.debugLineNum = 83;BA.debugLine="btn_cell9.Text = \"O\"";
mostCurrent._btn_cell9.setText(BA.ObjectToCharSequence("O"));
 //BA.debugLineNum = 84;BA.debugLine="turn = True";
_turn = anywheresoftware.b4a.keywords.Common.True;
 };
 //BA.debugLineNum = 86;BA.debugLine="End Sub";
return "";
}
public static String  _btn_winfor1_click() throws Exception{
int _score = 0;
 //BA.debugLineNum = 72;BA.debugLine="Sub btn_winfor1_Click";
 //BA.debugLineNum = 73;BA.debugLine="Dim score As Int = lbl_p1point.Text + 1";
_score = (int) ((double)(Double.parseDouble(mostCurrent._lbl_p1point.getText()))+1);
 //BA.debugLineNum = 74;BA.debugLine="lbl_p1point.Text = score";
mostCurrent._lbl_p1point.setText(BA.ObjectToCharSequence(_score));
 //BA.debugLineNum = 75;BA.debugLine="clear_all";
_clear_all();
 //BA.debugLineNum = 76;BA.debugLine="End Sub";
return "";
}
public static String  _btn_winfor2_click() throws Exception{
int _score = 0;
 //BA.debugLineNum = 66;BA.debugLine="Sub btn_winfor2_Click";
 //BA.debugLineNum = 67;BA.debugLine="Dim score As Int = lbl_p2point.Text + 1";
_score = (int) ((double)(Double.parseDouble(mostCurrent._lbl_p2point.getText()))+1);
 //BA.debugLineNum = 68;BA.debugLine="lbl_p2point.Text = score";
mostCurrent._lbl_p2point.setText(BA.ObjectToCharSequence(_score));
 //BA.debugLineNum = 69;BA.debugLine="clear_all";
_clear_all();
 //BA.debugLineNum = 70;BA.debugLine="End Sub";
return "";
}
public static String  _clear_all() throws Exception{
 //BA.debugLineNum = 54;BA.debugLine="Sub clear_all";
 //BA.debugLineNum = 55;BA.debugLine="btn_cell1.Text = \"\"";
mostCurrent._btn_cell1.setText(BA.ObjectToCharSequence(""));
 //BA.debugLineNum = 56;BA.debugLine="btn_cell2.Text = \"\"";
mostCurrent._btn_cell2.setText(BA.ObjectToCharSequence(""));
 //BA.debugLineNum = 57;BA.debugLine="btn_cell3.Text = \"\"";
mostCurrent._btn_cell3.setText(BA.ObjectToCharSequence(""));
 //BA.debugLineNum = 58;BA.debugLine="btn_cell4.Text = \"\"";
mostCurrent._btn_cell4.setText(BA.ObjectToCharSequence(""));
 //BA.debugLineNum = 59;BA.debugLine="btn_cell5.Text = \"\"";
mostCurrent._btn_cell5.setText(BA.ObjectToCharSequence(""));
 //BA.debugLineNum = 60;BA.debugLine="btn_cell6.Text = \"\"";
mostCurrent._btn_cell6.setText(BA.ObjectToCharSequence(""));
 //BA.debugLineNum = 61;BA.debugLine="btn_cell7.Text = \"\"";
mostCurrent._btn_cell7.setText(BA.ObjectToCharSequence(""));
 //BA.debugLineNum = 62;BA.debugLine="btn_cell8.Text = \"\"";
mostCurrent._btn_cell8.setText(BA.ObjectToCharSequence(""));
 //BA.debugLineNum = 63;BA.debugLine="btn_cell9.Text = \"\"";
mostCurrent._btn_cell9.setText(BA.ObjectToCharSequence(""));
 //BA.debugLineNum = 64;BA.debugLine="End Sub";
return "";
}
public static String  _globals() throws Exception{
 //BA.debugLineNum = 21;BA.debugLine="Sub Globals";
 //BA.debugLineNum = 26;BA.debugLine="Private turn As Boolean = True";
_turn = anywheresoftware.b4a.keywords.Common.True;
 //BA.debugLineNum = 27;BA.debugLine="Private lbl_p1point As Label";
mostCurrent._lbl_p1point = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 28;BA.debugLine="Private lbl_p2point As Label";
mostCurrent._lbl_p2point = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 29;BA.debugLine="Private btn_cell1 As Button";
mostCurrent._btn_cell1 = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 30;BA.debugLine="Private btn_cell2 As Button";
mostCurrent._btn_cell2 = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 31;BA.debugLine="Private btn_cell3 As Button";
mostCurrent._btn_cell3 = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 32;BA.debugLine="Private btn_cell4 As Button";
mostCurrent._btn_cell4 = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 33;BA.debugLine="Private btn_cell5 As Button";
mostCurrent._btn_cell5 = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 34;BA.debugLine="Private btn_cell6 As Button";
mostCurrent._btn_cell6 = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 35;BA.debugLine="Private btn_cell7 As Button";
mostCurrent._btn_cell7 = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 36;BA.debugLine="Private btn_cell8 As Button";
mostCurrent._btn_cell8 = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 37;BA.debugLine="Private btn_cell9 As Button";
mostCurrent._btn_cell9 = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 38;BA.debugLine="End Sub";
return "";
}

public static void initializeProcessGlobals() {
    
    if (main.processGlobalsRun == false) {
	    main.processGlobalsRun = true;
		try {
		        main._process_globals();
		
        } catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
}public static String  _process_globals() throws Exception{
 //BA.debugLineNum = 15;BA.debugLine="Sub Process_Globals";
 //BA.debugLineNum = 19;BA.debugLine="End Sub";
return "";
}
}
