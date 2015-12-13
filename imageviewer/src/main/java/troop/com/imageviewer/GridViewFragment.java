package troop.com.imageviewer;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

import com.defcomk.jni.libraw.RawUtils;
import com.troop.freedcam.utils.StringUtils;
import com.troop.marshmallowpermission.MPermissions;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by troop on 11.12.2015.
 */
public class GridViewFragment extends Fragment implements AdapterView.OnItemClickListener
{
    GridView gridView;
    View view;
    private ImageAdapter mPagerAdapter;
    FileHolder[] files;
    int mImageThumbSize = 0;
    CacheHelper cacheHelper;
    final String TAG = GridViewFragment.class.getSimpleName();
    private ViewStates currentViewState = ViewStates.normal;
    private Button deleteButton;
    private Button gobackButton;
    final String savedInstanceString = "lastpath";
    String savedInstanceFilePath;


    public enum ViewStates
    {
        normal,
        selection,
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.gridviewfragment, container, false);
        this.gridView = (GridView) view.findViewById(R.id.gridView);
        gridView.setOnItemClickListener(this);
        mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
        deleteButton = (Button)view.findViewById(R.id.button_deltePics);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (currentViewState) {
                    case normal:
                        setViewMode(ViewStates.selection);
                        break;
                    case selection:
                        //check if files are selceted
                        boolean hasfilesSelected = false;
                        for (FileHolder f : files) {
                            if (f.IsSelected()) {
                                hasfilesSelected = true;
                                break;
                            }

                        }
                        //if no files selected skip dialog
                        if (!hasfilesSelected)
                            break;
                        //else show dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage("Delete File?").setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).show();
                        setViewMode(ViewStates.normal);
                        break;
                }
            }
        });

        gobackButton = (Button)view.findViewById(R.id.button_goback);
        gobackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (files != null && files.length > 0)
                {
                    String topPath = files[0].getFile().getParentFile().getParentFile().getAbsolutePath()+ "/";
                    String inter = StringUtils.GetInternalSDCARD() + StringUtils.DCIMFolder;
                    String external = StringUtils.GetExternalSDCARD() + StringUtils.DCIMFolder;

                    if (topPath.equals(inter)
                            || topPath.equals(external))
                        loadDefaultFolders();
                    else
                    {
                        loadFiles(files[0].getFile());
                        savedInstanceFilePath = files[0].getFile().getAbsolutePath();
                    }
                }
            }
        });



        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            savedInstanceFilePath = (String) savedInstanceState.get(savedInstanceString);

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(savedInstanceString, savedInstanceFilePath);
        super.onSaveInstanceState(outState);
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:

                    for (FileHolder f: files)
                    {
                        if (f.IsSelected())
                        {
                            boolean d = f.getFile().delete();
                            Log.d(TAG,"File delted:" + f.getFile().getName() + " :" + d);
                        }
                    }
                    loadFiles();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };



    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cacheHelper = new CacheHelper(getActivity());
        if (Build.VERSION.SDK_INT > 22 &&getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            MPermissions.requestSDPermission(this);
        }
        if(savedInstanceState != null){
            savedInstanceFilePath = (String) savedInstanceState.get(savedInstanceString);

        }



    }

    @Override
    public void onResume() {
        super.onResume();
        if (savedInstanceFilePath == null)
            loadDefaultFolders();
        else
            loadFiles(new File(savedInstanceFilePath));
    }

    private void loadFiles()
    {
        File[] f = ScreenSlideFragment.loadFilePaths();
        files =  new FileHolder[f.length];
        for (int i =0; i< f.length; i++)
        {
            files[i] = new FileHolder(f[i]);
        }

    }

    private void loadDefaultFolders()
    {
        File internalSDCIM = new File(StringUtils.GetInternalSDCARD() + StringUtils.DCIMFolder);
        File externalSDCIM = new File(StringUtils.GetExternalSDCARD() + StringUtils.DCIMFolder);
        ArrayList<FileHolder> list = new ArrayList<FileHolder>();
        for (int i = 0; i< internalSDCIM.listFiles().length; i++)
        {
            list.add(new FileHolder(internalSDCIM.listFiles()[i]));
        }
        try {
            for (int i = 0; i< externalSDCIM.listFiles().length; i++)
            {
                list.add(new FileHolder(externalSDCIM.listFiles()[i]));
            }
        }
        catch (Exception ex) {
            Log.d(TAG, "No external SD!");
        }
        files = new FileHolder[list.size()];
        list.toArray(files);
        mPagerAdapter = new ImageAdapter(getContext());
        gridView.setAdapter(mPagerAdapter);
    }

    private void loadFiles(File file)
    {
        ArrayList<FileHolder> list = new ArrayList<FileHolder>();
        for (int i = 0; i< file.listFiles().length; i++)
        {
            list.add(new FileHolder(file.listFiles()[i]));
        }
        files = new FileHolder[list.size()];
        list.toArray(files);
        mPagerAdapter = new ImageAdapter(getContext());
        gridView.setAdapter(mPagerAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        switch (currentViewState)
        {
            case normal:
                if (!files[position].IsFolder()) {
                    final Intent i = new Intent(getActivity(), ScreenSlideActivity.class);
                    i.putExtra(ScreenSlideActivity.EXTRA_IMAGE, position);
                    startActivity(i);
                }
                else
                {
                    loadFiles(files[position].getFile());
                    savedInstanceFilePath = files[position].getFile().getParentFile().getAbsolutePath();
                }
                break;
            case selection:
                if (files[position].IsSelected())
                    files[position].SetSelected(false);
                else
                    files[position].SetSelected(true);
        }
    }

    private class ImageAdapter extends BaseAdapter {
        private final Context mContext;

        public ImageAdapter(Context context) {
            super();
            mContext = context;
        }

        @Override
        public int getCount() {
            return files.length;
        }

        @Override
        public Object getItem(int position) {
            return files[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            GridImageView imageView;
            if (convertView == null) { // if it's not recycled, initialize some attributes
                imageView = new GridImageView(mContext);
                imageView.SetEventListner(files[position]);
                files[position].SetViewState(currentViewState);
                imageView.setLayoutParams(new GridView.LayoutParams(
                        AbsoluteLayout.LayoutParams.MATCH_PARENT, AbsoluteLayout.LayoutParams.MATCH_PARENT));
            } else {
                imageView = (GridImageView) convertView;
                imageView.SetEventListner(files[position]);
                files[position].SetViewState(currentViewState);
            }
            Log.d(TAG, "pos:" + position +  "imageviewState: " + files[position].GetCurrentViewState()+ "/GridState:" + currentViewState + "filename:" + files[position].getFile().getName()+
                    "ischecked:" + files[position].IsSelected());
            loadBitmap(files[position].getFile(), imageView); // Load image into ImageView
            return imageView;
        }
    }

    private static BitmapWorkerTask getBitmapWorkerTask(GridImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    public void loadBitmap(File file, GridImageView imageView)
    {
        if (cancelPotentialWork(file, imageView))
        {
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
            if (!file.isDirectory())
            {
                final AsyncDrawable asyncDrawable =
                        new AsyncDrawable(getResources(), ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.noimage), mImageThumbSize, mImageThumbSize), task);
                imageView.setImageDrawable(asyncDrawable);
            }
            else
            {
                final AsyncDrawable asyncDrawable =
                        new AsyncDrawable(getResources(), ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(getResources(), R.drawable.folder), mImageThumbSize, mImageThumbSize), task);
                imageView.setImageDrawable(asyncDrawable);
            }
            String f = file.getName();
            if (!file.isDirectory())
                imageView.SetFileEnding(f.substring(f.length()-3));
            else
                imageView.SetFileEnding(f);
            task.execute(file);
        }
    }


    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap,
                             BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                    new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    public static boolean cancelPotentialWork(File file, GridImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final File bitmapData = bitmapWorkerTask.file;
            if (bitmapData != file) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    class BitmapWorkerTask extends AsyncTask<File, Void, Bitmap> {
        private final WeakReference<GridImageView> imageViewReference;
        private File file;

        public BitmapWorkerTask(GridImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<GridImageView>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(File... params) {
            file = params[0];
            return getBitmap(file);
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewReference != null && bitmap != null) {
                final GridImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    private Bitmap getBitmap(File file)
    {
        if (!file.getAbsolutePath().endsWith("jpg")
                && !file.getAbsolutePath().endsWith("dng")
                && !file.getAbsolutePath().endsWith("mp4")
                && !file.getAbsolutePath().endsWith("raw")
                && !file.getAbsolutePath().endsWith("jps"))
            return null;

        Bitmap response = cacheHelper.getBitmapFromMemCache(file.getName());
        if (response == null)
        {
            //Log.d(TAG,"No image in memory try from disk");
            response = cacheHelper.getBitmapFromDiskCache(file.getName());
        }
        if (response == null)
        {
            //Log.d(TAG,"No image in thumbcache try from disk");
            if (file.getAbsolutePath().endsWith(".jpg")) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 20;
                response = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
                response = ThumbnailUtils.extractThumbnail(response, mImageThumbSize, mImageThumbSize);
                if (response != null) {
                    cacheHelper.addBitmapToMemoryCache(file.getName(), response);
                    cacheHelper.addBitmapToCache(file.getName(), response);
                }

            } else if (file.getAbsolutePath().endsWith(".mp4")) {
                response = ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
                response = ThumbnailUtils.extractThumbnail(response, mImageThumbSize, mImageThumbSize);
                if (response != null)
                {
                    cacheHelper.addBitmapToCache(file.getName(), response);
                    cacheHelper.addBitmapToMemoryCache(file.getName(), response);
                }
            } else if (file.getAbsolutePath().endsWith(".dng") || file.getAbsolutePath().endsWith(".raw")) {
                try {


                    response = RawUtils.UnPackRAW(file.getAbsolutePath());
                    if (response != null)
                        response.setHasAlpha(true);
                    response = ThumbnailUtils.extractThumbnail(response, mImageThumbSize, mImageThumbSize);
                    if (response != null) {
                        cacheHelper.addBitmapToMemoryCache(file.getName(), response);
                        cacheHelper.addBitmapToCache(file.getName(), response);
                    }
                } catch (IllegalArgumentException ex) {
                    response = null;

                }
            }
        }


        return response;
    }

    private void setViewMode(ViewStates viewState)
    {
        this.currentViewState = viewState;
        for (int i = 0; i< files.length; i++)
        {
            FileHolder f = files[i];
            f.SetViewState(viewState);

        }
    }

}
