package com.example.user.hw7;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.os.Handler;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.os.Message;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private HotelArrayAdapter adapter = null;
    private static final int LIST_HOTELS = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView lvHotels = (ListView)findViewById(R.id.listview_hotel);
        HotelArrayAdapter adapter = new HotelArrayAdapter(this, new ArrayList<Hotel>());
        lvHotels.setAdapter(adapter);
        getImFromFirebase();
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LIST_HOTELS: {
                    List<Hotel> hotels = (List<Hotel>)msg.obj;
                    refreshHotelList(hotels);
                    break;
                }
            }
        }
    };

    public class FirebaseThread extends  Thread{
        private DataSnapshot dataSnapshot;
        public  FirebaseThread(DataSnapshot dataSnapshot) {
            this.dataSnapshot = dataSnapshot;
        }

        @Override
        public  void run() {
            List<Hotel> lsHotels = new ArrayList<>();
            for(DataSnapshot ds : dataSnapshot.getChildren()) {
                DataSnapshot dsSname = ds.child("Name");
                DataSnapshot dsAdd = ds.child("Add");
                DataSnapshot dsTel = ds.child("Tel");

                String shelterName = (String)dsSname.getValue();
                String address = (String)dsAdd.getValue();
                String tel = (String)dsTel.getValue();

                DataSnapshot dsImg = ds.child("Picture1");
                String imgUrl = (String)dsImg.getValue();
                Bitmap hotelImg = getImgBitmap(imgUrl);

                Hotel ahotel = new Hotel();
                ahotel.setName(shelterName);
                ahotel.setAddress(address);
                ahotel.setTel(tel);
                ahotel.setImgUrl(hotelImg);
                lsHotels.add(ahotel);


                Log.v("Hotel", shelterName);
                Message msg = new Message();
                msg.what = LIST_HOTELS;
                msg.obj = lsHotels;
                handler.sendMessage(msg);
            }
        }

        private  Bitmap getImgBitmap(String imgUrl) {
            try {
                URL url = new URL(imgUrl);
                Bitmap bm = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                return bm;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    private void refreshHotelList(List<Hotel> hotels) {
        adapter.clear();
        adapter.addAll(hotels);

    }

    private void getImFromFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
              /* for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    DataSnapshot dsSname = ds.child("Name");
                    DataSnapshot dsAdd = ds.child("Add");
                    DataSnapshot dsTel = ds.child("Tel");

                    String shelterName = (String)dsSname.getValue();
                    String address = (String)dsAdd.getValue();
                    String tel = (String)dsAdd.getValue();

                    DataSnapshot dsImg = ds.child("Picture1");
                    String imgUrl = (String)dsImg.getValue();
                    Bitmap hotelImg = getImgBitmap(imgUrl);


                    Log.v("Hotel", shelterName);
                }*/
                new FirebaseThread(dataSnapshot).start();
            }

           /* private  Bitmap getImgBitmap(String imgUrl) {
                try {
                    URL url = new URL(imgUrl);
                    Bitmap bm = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    return bm;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }*/



            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.v("Hotel", databaseError.getMessage());
            }
        });
    }
}
