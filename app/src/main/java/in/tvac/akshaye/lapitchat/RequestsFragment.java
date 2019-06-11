package in.tvac.akshaye.lapitchat;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private RecyclerView mRequestList;
    private FirebaseUser mCurrent_user;
    private DatabaseReference mFriendRequestDatabase;
    private DatabaseReference mUsersDatabase,mRootRef;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    private View mMainView;



    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView= inflater.inflate(R.layout.fragment_requests, container, false);

        mRequestList = (RecyclerView) mMainView.findViewById(R.id.requestlist);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();
        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();

        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(mCurrent_user_id);
       // mFriendRequestDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
      //  mUsersDatabase.keepSynced(true);
        mRootRef = FirebaseDatabase.getInstance().getReference();


        mRequestList.setHasFixedSize(true);
        mRequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        return  mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<FriendRequest,MyRequestListViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<FriendRequest, MyRequestListViewHolder>(
                FriendRequest.class,
                R.layout.reqsingle,
                MyRequestListViewHolder.class,
                mFriendRequestDatabase


        ) {
            @Override
            protected void populateViewHolder(final MyRequestListViewHolder viewHolder, FriendRequest model, int position) {

                String reqtype=model.getRequest_type();
                final String user_id = getRef(position).getKey();

                final String list_user_id = getRef(position).getKey();
                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("name").getValue().toString();
                     String userStatus=dataSnapshot.child("status").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                        if(dataSnapshot.hasChild("online")) {

                            String userOnline = dataSnapshot.child("online").getValue().toString();
                           viewHolder.setUserOnline(userOnline);

                        }
                        viewHolder.setNameandStatus(userName,userStatus);
                        viewHolder.setUserImage(userThumb, getContext());

                        viewHolder.accept.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {


                                final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                                Map friendsMap = new HashMap();
                                friendsMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id + "/date", currentDate);
                                friendsMap.put("Friends/" + user_id + "/"  + mCurrent_user.getUid() + "/date", currentDate);


                                friendsMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id, null);
                                friendsMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid(), null);


                                mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {


                                        if(databaseError == null){

                                      viewHolder.accept.setText("REQUEST ACCEPTED");

                                      viewHolder.decline.setVisibility(View.INVISIBLE);
                                        } else {

                                            String error = databaseError.getMessage();

                                          //  Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();


                                        }

                                    }
                                });

                            }
                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };
        mRequestList.setAdapter(firebaseRecyclerAdapter);
      //  Toast.makeText(getContext(), ""+mRequestList.getChildCount(), Toast.LENGTH_SHORT).show();
//   if(mRequestList.getChildCount()==0) {
//       mRequestList.setBackgroundResource(R.mipmap.oops);
//   }
    }

    public static class MyRequestListViewHolder extends RecyclerView.ViewHolder{
        View mView;
        Button accept,decline;

        public MyRequestListViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            accept= (Button) itemView.findViewById(R.id.requestlistacceptbutton);
            decline= (Button) itemView.findViewById(R.id.DECLINE);


        }
        public void setNameandStatus(String name,String status){

            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
            TextView userStatusView = (TextView) mView.findViewById(R.id.user_single_status);


        }
        public void setUserImage(String thumb_image, Context ctx){

            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.user_single_image);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.default_avatar).into(userImageView);

        }
        public void setUserOnline(String online_status) {

            ImageView userOnlineView = (ImageView) mView.findViewById(R.id.user_single_online_icon);

            if(online_status.equals("true")){

                userOnlineView.setVisibility(View.VISIBLE);

            } else {

                userOnlineView.setVisibility(View.INVISIBLE);

            }

        }
    }

}
