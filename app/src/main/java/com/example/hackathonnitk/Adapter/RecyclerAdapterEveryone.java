package com.example.hackathonnitk.Adapter;


import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hackathonnitk.Algorithms.CachedThumbnail;
import com.example.hackathonnitk.Algorithms.ImageConverter;
import com.example.hackathonnitk.Algorithms.StoreImage;
import com.example.hackathonnitk.ConstantsIt;
import com.example.hackathonnitk.R;
import com.example.hackathonnitk.model.CacheParams;
import com.example.hackathonnitk.model.ImageUploadInfo;
import com.github.clans.fab.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class RecyclerAdapterEveryone extends RecyclerView.Adapter<RecyclerAdapterEveryone.ViewHolder> {
    Context context;
    ProgressDialog progressDialog;
    List<ImageUploadInfo> MainImageUploadInfoList;
    String username;
    Bitmap bitmap = null;
    String cacheDir = null;

    public RecyclerAdapterEveryone(Context context, List<ImageUploadInfo> TempList,String username) {
        this.MainImageUploadInfoList = TempList;
        this.context = context;
        this.username=username;
        this.bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.image);
        this.cacheDir = context.getExternalCacheDir().toString();
    }

    @Override
    public RecyclerAdapterEveryone.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item, parent, false);
        RecyclerAdapterEveryone.ViewHolder viewHolder = new RecyclerAdapterEveryone.ViewHolder(view);
        return viewHolder;
    }



    @Override
    public void onBindViewHolder(final RecyclerAdapterEveryone.ViewHolder holder, final int position) {
        ImageUploadInfo UploadInfo = MainImageUploadInfoList.get(position);
        final String imagenameis=UploadInfo.getImageName();
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.i("CardView","True");
                PopupMenu popupMenu=new PopupMenu(context,v);
                popupMenu.inflate(R.menu.everyone);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id2=item.getItemId();

                        if(id2==R.id.action_download){

                            String imagenameisfi = imagenameis.replaceAll("\n", "");
                            if(imagenameisfi.charAt(imagenameisfi.length()-1)=='4'){
                                new RecyclerAdapterEveryone.VideoSaveAsync().execute(imagenameisfi);
                                Log.i("Click on","Video");
                            }
                            else{
                                Log.i("Click on","Image");
                                new RecyclerAdapterEveryone.ImageSave().execute(imagenameisfi);
                            }

                        }
                        if(id2==R.id.action_delete){
                            new RecyclerAdapterEveryone.DeleteImage().execute(imagenameis);
                            holder.cardView.setVisibility(View.GONE);
                        }
                        return false;
                    }
                });
                popupMenu.show();
                return false;
            }
        });
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu=new PopupMenu(context,v);
                popupMenu.inflate(R.menu.everyone);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id2=item.getItemId();

                        if(id2==R.id.action_download){
                            if(imagenameis.charAt(imagenameis.length()-1)=='4'){
                                new RecyclerAdapterEveryone.VideoSaveAsync().execute(imagenameis);
                                Log.i("Click on","Video");
                            }
                            else{
                                Log.i("Click on","Image");
                                new RecyclerAdapterEveryone.ImageSave().execute(imagenameis);
                            }

                        }
                        if(id2==R.id.action_delete){
                            new RecyclerAdapterEveryone.DeleteImage().execute(imagenameis);
                            holder.cardView.setVisibility(View.GONE);
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        holder.imageNameTextView.setText(imagenameis);
        CachedThumbnail tmp = new CachedThumbnail();
        (tmp).execute(new CacheParams(holder.imageView, cacheDir+"/"+imagenameis));

    }

    @Override
    public int getItemCount() {

        return MainImageUploadInfoList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public TextView imageNameTextView;
        public CardView cardView;
        public FloatingActionButton floatingActionButton;
        public ImageView imageView;
        public ViewHolder(View itemView) {
            super(itemView);
            cardView=itemView.findViewById(R.id.cardview1);
            imageNameTextView=itemView.findViewById(R.id.imagename);
            floatingActionButton=itemView.findViewById(R.id.menudis);
            this.imageView = itemView.findViewById(R.id.imageviewrecycler);
        }

    }

    private class ImageSave extends AsyncTask<String,Void,Void> {

        String filepath;

        @Override
        protected Void doInBackground(String... strings) {
            try {
                URL url = new URL(ConstantsIt.LOCALURLGETIMAGE);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setRequestMethod("POST");
                OutputStream os=urlConnection.getOutputStream();

                DataOutputStream wr = new DataOutputStream(os);

                try {
                    JSONObject obj = new JSONObject();
                    obj.put("image",strings[0]);
                    wr.writeBytes(obj.toString());
                    Log.i("JSON Input", obj.toString());
                    wr.flush();
                    wr.close();
                    os.close();
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }

                int responseCode = urlConnection.getResponseCode();
                Log.i("RsponseCode", "is "+responseCode);

                if(responseCode == HttpURLConnection.HTTP_OK){
                    String server_response = readStream(urlConnection.getInputStream());
                    Log.i("Response",server_response);
                    ImageConverter imageConverter=new ImageConverter();
                    Bitmap bitmap=imageConverter.getBitmapFromString(server_response);
                    StoreImage storeImage=new StoreImage();
                    filepath=storeImage.storeImage(context,bitmap);

                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Downloading...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(filepath!=null){
                Toast.makeText(context,"File Stored at: "+filepath,Toast.LENGTH_LONG).show();
                Log.i("File Stored at:- ",filepath);
                // notificationshow();
            }

            else Toast.makeText(context,"Error in Saving file ",Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
            super.onPostExecute(aVoid);
        }

    }

    private class DeleteImage extends AsyncTask<String,Void,Void>{

        String filepath;

        @Override
        protected Void doInBackground(String... strings) {
            try {
                URL url = new URL(ConstantsIt.LOCALURLDELETEIMAGE);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setRequestMethod("POST");
                OutputStream os=urlConnection.getOutputStream();

                DataOutputStream wr = new DataOutputStream(os);

                try {
                    JSONObject obj = new JSONObject();
                    obj.put("user" , username);
                    obj.put("image",strings[0]);

                    wr.writeBytes(obj.toString());
                    Log.i("JSON Input", obj.toString());
                    wr.flush();
                    wr.close();
                    os.close();
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }

                int responseCode = urlConnection.getResponseCode();
                Log.i("RsponseCode", "is "+responseCode);

                if(responseCode == HttpURLConnection.HTTP_OK){
                    String server_response = readStream(urlConnection.getInputStream());
                    Log.i("Response",server_response);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Removing...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(context,"File Deleted",Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
            super.onPostExecute(aVoid);
        }
    }

    private class VideoSaveAsync extends AsyncTask<String,Void,Void>{

        String filepath;

        @Override
        protected Void doInBackground(String... strings) {
            try {
                URL url = new URL(ConstantsIt.LOCALURLDOWNLOAD);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setRequestMethod("POST");
                OutputStream os=urlConnection.getOutputStream();

                DataOutputStream wr = new DataOutputStream(os);

                try {
                    JSONObject obj = new JSONObject();
                    obj.put("user" , username);
                    obj.put("name",strings[0]);


                    wr.writeBytes(obj.toString());
                    Log.i("JSON Input", obj.toString());
                    wr.flush();
                    wr.close();
                    os.close();
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
                int responseCode = urlConnection.getResponseCode();
                Log.i("RsponseCode", "is "+responseCode);
                if(responseCode == HttpURLConnection.HTTP_OK){
                    String server_response = readStream(urlConnection.getInputStream());
                    Log.i("Response",server_response);
                    StoreImage storeImage=new StoreImage();
                    filepath=storeImage.saveVideo(context,server_response);

                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Downloading...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(filepath!=null){
                Toast.makeText(context,"Video Stored at: "+filepath,Toast.LENGTH_LONG).show();
            }

            else Toast.makeText(context,"Error in Saveing file ",Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
            super.onPostExecute(aVoid);
        }

    }


    public static String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }

}

