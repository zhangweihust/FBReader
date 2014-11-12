package com.cmmobi.reader;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.SelectionBookmarkAction;
import org.geometerplus.android.fbreader.SelectionHidePanelAction;
import org.geometerplus.android.fbreader.SelectionShowPanelAction;
import org.geometerplus.android.fbreader.SetScreenOrientationAction;
import org.geometerplus.android.fbreader.ShowBookmarksAction;
import org.geometerplus.android.fbreader.ShowCancelMenuAction;
import org.geometerplus.android.fbreader.ShowMenuAction;
import org.geometerplus.android.fbreader.ShowTOCAction;
import org.geometerplus.android.fbreader.api.FBReaderIntents;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.network.sync.SyncData.ServerBookInfo;
import org.geometerplus.zlibrary.core.application.ZLApplicationWindow;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.options.Config;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.view.ZLViewWidget;
import org.geometerplus.zlibrary.ui.android.error.ErrorKeys;
import org.geometerplus.zlibrary.ui.android.view.AndroidFontUtil;
import org.geometerplus.zlibrary.ui.android.view.ZLAndroidWidget;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

public class CmmobiReaderActivity extends Activity implements ZLApplicationWindow, FBReaderApp.Notifier, FBReaderInterface, Callback{
	private static final int HANDLER_FALG_OPEN_BOOK = 0x01839178;
	private RelativeLayout myRootView;
	private ZLAndroidWidget myMainView;
	
	private FBReaderApp myFBReaderApp;
	private volatile Book myBook;
	
	private Handler handler;
	
	/*final DataService.Connection DataConnection = new DataService.Connection();*/
	
	@Override
	protected void onCreate(Bundle icicle){
		super.onCreate(icicle);
		
		handler = new Handler(this);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		
		myRootView = (RelativeLayout)findViewById(R.id.root_view);
		myMainView = (ZLAndroidWidget)findViewById(R.id.main_view);
		
		myFBReaderApp = (FBReaderApp)FBReaderApp.Instance();
		if (myFBReaderApp == null) {
			myFBReaderApp = new FBReaderApp(new BookCollectionShadow());
		}
		getCollection().bindToService(this, null);
		myBook = null;

		myFBReaderApp.setWindow(this);
		myFBReaderApp.initWindow();
		
		getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN
			);
		
//		myFBReaderApp.addAction(ActionCode.SHOW_LIBRARY, new ShowLibraryAction(this, myFBReaderApp));
//		myFBReaderApp.addAction(ActionCode.SHOW_PREFERENCES, new ShowPreferencesAction(this, myFBReaderApp));
//		myFBReaderApp.addAction(ActionCode.SHOW_BOOK_INFO, new ShowBookInfoAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SHOW_TOC, new ShowTOCAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SHOW_BOOKMARKS, new ShowBookmarksAction(this, myFBReaderApp));
//		myFBReaderApp.addAction(ActionCode.SHOW_NETWORK_LIBRARY, new ShowNetworkLibraryAction(this, myFBReaderApp));

		myFBReaderApp.addAction(ActionCode.SHOW_MENU, new ShowMenuAction(this, myFBReaderApp));
//		myFBReaderApp.addAction(ActionCode.SHOW_NAVIGATION, new ShowNavigationAction(this, myFBReaderApp));
//		myFBReaderApp.addAction(ActionCode.SEARCH, new SearchAction(this, myFBReaderApp));
//		myFBReaderApp.addAction(ActionCode.SHARE_BOOK, new ShareBookAction(this, myFBReaderApp));

		myFBReaderApp.addAction(ActionCode.SELECTION_SHOW_PANEL, new SelectionShowPanelAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SELECTION_HIDE_PANEL, new SelectionHidePanelAction(this, myFBReaderApp));
//		myFBReaderApp.addAction(ActionCode.SELECTION_COPY_TO_CLIPBOARD, new SelectionCopyAction(this, myFBReaderApp));
//		myFBReaderApp.addAction(ActionCode.SELECTION_SHARE, new SelectionShareAction(this, myFBReaderApp));
//		myFBReaderApp.addAction(ActionCode.SELECTION_TRANSLATE, new SelectionTranslateAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SELECTION_BOOKMARK, new SelectionBookmarkAction(this, myFBReaderApp));

//		myFBReaderApp.addAction(ActionCode.PROCESS_HYPERLINK, new ProcessHyperlinkAction(this, myFBReaderApp));
//		myFBReaderApp.addAction(ActionCode.OPEN_VIDEO, new OpenVideoAction(this, myFBReaderApp));

		myFBReaderApp.addAction(ActionCode.SHOW_CANCEL_MENU, new ShowCancelMenuAction(this, myFBReaderApp));

		myFBReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_SYSTEM, new SetScreenOrientationAction(this, myFBReaderApp, ZLibrary.SCREEN_ORIENTATION_SYSTEM));
		myFBReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_SENSOR, new SetScreenOrientationAction(this, myFBReaderApp, ZLibrary.SCREEN_ORIENTATION_SENSOR));
		myFBReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_PORTRAIT, new SetScreenOrientationAction(this, myFBReaderApp, ZLibrary.SCREEN_ORIENTATION_PORTRAIT));
		myFBReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_LANDSCAPE, new SetScreenOrientationAction(this, myFBReaderApp, ZLibrary.SCREEN_ORIENTATION_LANDSCAPE));
		if (ZLibrary.Instance().supportsAllOrientations()) {
			myFBReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_REVERSE_PORTRAIT, new SetScreenOrientationAction(this, myFBReaderApp, ZLibrary.SCREEN_ORIENTATION_REVERSE_PORTRAIT));
			myFBReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_REVERSE_LANDSCAPE, new SetScreenOrientationAction(this, myFBReaderApp, ZLibrary.SCREEN_ORIENTATION_REVERSE_LANDSCAPE));
		}
//		myFBReaderApp.addAction(ActionCode.OPEN_WEB_HELP, new OpenWebHelpAction(this, myFBReaderApp));
//		myFBReaderApp.addAction(ActionCode.INSTALL_PLUGINS, new InstallPluginsAction(this, myFBReaderApp));
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		myFBReaderApp.getViewWidget().repaint();
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		
//		openBook(getIntent(), null, true);//src open
//		Book book = openBook("/sdcard/epubtemp/christmas_carol_parallel_4.epub", null, true);
		
//		BookModel model = myFBReaderApp.Model;
//		if (model == null || model.Book == null) {
//			return;
//		}
		
//		onPreferencesUpdate(myFBReaderApp.Collection.getBookById(model.Book.getId()));
//		onPreferencesUpdate(book);
		handler.sendEmptyMessageDelayed(HANDLER_FALG_OPEN_BOOK, 1000);
		
		SetScreenOrientationAction.setOrientation(this, ZLibrary.Instance().getOrientationOption().getValue());
		

	}
	
	@Override
	protected void onPause() {
		myFBReaderApp.stopTimer();
		myFBReaderApp.onWindowClosing();
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void onLowMemory() {
		myFBReaderApp.onWindowClosing();
		super.onLowMemory();
	}
	
	
	@Override
	public void showMissingBookNotification(ServerBookInfo info) {
		final String errorMessage =
				ZLResource.resource("errorMessage").getResource("bookIsMissing").getValue()
					.replace("%s", info.Title);

			final NotificationManager notificationManager =
				(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
			final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(), 0);
			final Notification notification = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.fbreader)
				.setTicker(info.Title)
				.setContentTitle(info.Title)
				.setContentText(errorMessage)
				.setContentIntent(pendingIntent)
				.setAutoCancel(true)
				.build();
			notificationManager.notify(0, notification);
		}

	@Override
	public void setWindowTitle(final String title) {
		// TODO Auto-generated method stub
		runOnUiThread(new Runnable() {
			public void run() {
				setTitle(title);
			}
		});
	}

	@Override
	public void showErrorMessage(String key) {
		// TODO Auto-generated method stub
		UIUtil.showErrorMessage(this, key);
	}

	@Override
	public void showErrorMessage(String key, String parameter) {
		UIUtil.showErrorMessage(this, key, parameter);
	}

	@Override
	public FBReaderApp.SynchronousExecutor createExecutor(String key) {
		return UIUtil.createExecutor(this, key);
	}

	@Override
	public void processException(Exception exception) {
		exception.printStackTrace();

		final Intent intent = new Intent(
			FBReaderIntents.Action.ERROR,
			new Uri.Builder().scheme(exception.getClass().getSimpleName()).build()
		);
		intent.setPackage(FBReaderIntents.DEFAULT_PACKAGE);
		intent.putExtra(ErrorKeys.MESSAGE, exception.getMessage());
		final StringWriter stackTrace = new StringWriter();
		exception.printStackTrace(new PrintWriter(stackTrace));
		intent.putExtra(ErrorKeys.STACKTRACE, stackTrace.toString());
		/*
		if (exception instanceof BookReadingException) {
			final ZLFile file = ((BookReadingException)exception).File;
			if (file != null) {
				intent.putExtra("file", file.getPath());
			}
		}
		*/
		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			// ignore
			e.printStackTrace();
		}
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ZLViewWidget getViewWidget() {
		// TODO Auto-generated method stub
		return myMainView;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		finish();
		
	}

	@Override
	public int getBatteryLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void navigate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hideSelectionPanel() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showSelectionPanel() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getDataConnectionPort() {
		// TODO Auto-generated method stub
		return 0;/*DataConnection.getPort();*/
	}

	@Override
	public Activity getActivity() {
		// TODO Auto-generated method stub
		return this;
	}
	
	private void onPreferencesUpdate(Book book) {
		AndroidFontUtil.clearFontCache();
		myFBReaderApp.onBookUpdated(book);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return (myMainView != null && myMainView.onKeyDown(keyCode, event)) || super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return (myMainView != null && myMainView.onKeyUp(keyCode, event)) || super.onKeyUp(keyCode, event);
	}
	
	private synchronized Book openBook(String path, final Runnable action, boolean force){
		if (path != null) {
			myBook = createBookForFile(ZLFile.createFileByPath(path));
		}
		
		if (myBook != null) {
			ZLFile file = myBook.File;
			if (!file.exists()) {
				if (file.getPhysicalFile() != null) {
					file = file.getPhysicalFile();
				}
				UIUtil.showErrorMessage(this, "fileNotFound", file.getPath());
				myBook = null;
			}
		}
		
		myFBReaderApp.openBook(myBook, null, action, CmmobiReaderActivity.this);
		AndroidFontUtil.clearFontCache();
		
		return myBook;
	}
	
	///sdcard/epubtemp/christmas_carol_parallel_4.epub
	private synchronized void openBook(Intent intent, final Runnable action, boolean force) {
		if (!force && myBook != null) {
			return;
		}

		myBook = FBReaderIntents.getBookExtra(intent);
		final Bookmark bookmark = FBReaderIntents.getBookmarkExtra(intent);
		if (myBook == null) {
			final Uri data = intent.getData();
			if (data != null) {
				myBook = createBookForFile(ZLFile.createFileByPath(data.getPath()));
			}
		}
		if (myBook != null) {
			ZLFile file = myBook.File;
			if (!file.exists()) {
				if (file.getPhysicalFile() != null) {
					file = file.getPhysicalFile();
				}
				UIUtil.showErrorMessage(this, "fileNotFound", file.getPath());
				myBook = null;
			}
		}
		Config.Instance().runOnConnect(new Runnable() {
			public void run() {
				myFBReaderApp.openBook(myBook, bookmark, action, CmmobiReaderActivity.this);
				AndroidFontUtil.clearFontCache();
			}
		});
	}
	
	public static void openBookActivity(Context context, Book book, Bookmark bookmark) {
		final Intent intent = new Intent(context, FBReader.class)
			.setAction(FBReaderIntents.Action.VIEW)
			.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		FBReaderIntents.putBookExtra(intent, book);
		FBReaderIntents.putBookmarkExtra(intent, bookmark);
		context.startActivity(intent);
	}
	
	private BookCollectionShadow getCollection() {
		return (BookCollectionShadow)myFBReaderApp.Collection;
	}
	
	private Book createBookForFile(ZLFile file) {
		if (file == null) {
			return null;
		}
		Book book = myFBReaderApp.Collection.getBookByFile(file);
		if (book != null) {
			return book;
		}
		if (file.isArchive()) {
			for (ZLFile child : file.children()) {
				book = myFBReaderApp.Collection.getBookByFile(child);
				if (book != null) {
					return book;
				}
			}
		}
		return null;
	}

	@Override
	public boolean handleMessage(Message msg) {
		// TODO Auto-generated method stub
		switch(msg.what){
		case HANDLER_FALG_OPEN_BOOK:
			Book book = openBook("/sdcard/epubtemp/christmas_carol_parallel_4.epub", null, true);
			break;
		}
		return false;
	}


}
