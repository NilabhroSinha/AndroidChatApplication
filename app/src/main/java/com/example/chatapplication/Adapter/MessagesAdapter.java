package com.example.chatapplication.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapplication.Activity.ChatActivity;
import com.example.chatapplication.Activity.ZoomedChatImage;
import com.example.chatapplication.model.MessageModel;
import com.example.chatapplication.R;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter{

    Context context;
    ArrayList<MessageModel> arrayList;
    int ITEM_SEND = 1, ITEM_RECEIVE = 2, ITEM_SEND_IMAGE = 3, ITEM_RECEIVE_IMAGE = 4;
    String senderRoom, receiverRoom;
    AlertDialog.Builder builder;
    RecyclerView rv;
    FirebaseAuth auth;
    FirebaseUser firebaseCurrentUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    public MessagesAdapter(Context context, ArrayList<MessageModel> arrayList, String senderRoom, String receiverRoom, RecyclerView rv) {
        this.context = context;
        this.arrayList = arrayList;
        this.senderRoom = senderRoom;
        this.receiverRoom = receiverRoom;
        this.rv = rv;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == ITEM_SEND){
            view = LayoutInflater.from(context).inflate(R.layout.sender_message, parent, false);
            return new senderViewHolder(view);
        }
        else if(viewType == ITEM_SEND_IMAGE) {
            view = LayoutInflater.from(context).inflate(R.layout.image_message_right, parent, false);
            return new senderViewHolder(view);
        }
        else if(viewType == ITEM_RECEIVE_IMAGE) {
            view = LayoutInflater.from(context).inflate(R.layout.image_message_left, parent, false);
            return new receiverViewHolder(view);
        }
        else{
            view = LayoutInflater.from(context).inflate(R.layout.receiver_message, parent, false);
            return new receiverViewHolder(view);
        }

    }

    RecyclerView.OnItemTouchListener mOnItemTouchListener = new RecyclerView.OnItemTouchListener() {
        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            if (e.getAction() == MotionEvent.ACTION_DOWN && rv.getScrollState() == RecyclerView.SCROLL_STATE_SETTLING) {
                rv.findChildViewUnder(e.getX(), e.getY()).performClick();
                return true;
            }
            return false;
        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {}

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
    };

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        auth = FirebaseAuth.getInstance();
        firebaseCurrentUser = auth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("user");
        builder = new AlertDialog.Builder(context);

        MessageModel messageModel = arrayList.get(position);

        int reactions[] = new int[]{
                R.drawable.thumbs,
                R.drawable.heart,
                R.drawable.lol,
                R.drawable.openm,
                R.drawable.grinning,
                R.drawable.tear,
                R.drawable.grimacing
        };

        ReactionPopup popup = ReactionManager(reactions, holder, messageModel);           // Dealing with reactions


        if(holder.getClass() == senderViewHolder.class){
            senderViewHolder viewHolder = (senderViewHolder) holder;

            if(messageModel.getMessage() == null){
                Glide.with(context).load(messageModel.getChatImageId()).into(viewHolder.chatImageRight);

                viewHolder.chatImageRight.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        viewHolder.chatImageRight.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
                        DeleteMessage(messageModel);

                        return false;
                    }
                });

                viewHolder.chatImageRight.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ZoomedChatImage.class);
                        intent.putExtra("ReceiverImage", messageModel.getChatImageId());
                        context.startActivity(intent);
                    }
                });
            }
            else{
                viewHolder.senderID.setText(messageModel.getMessage());

                viewHolder.rightChatID.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        viewHolder.rightChatID.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
                        DeleteMessage(messageModel);

                        return false;
                    }
                });
            }

            long timestamp = messageModel.getTimeStamp();
            SimpleDateFormat sfd = new SimpleDateFormat("hh:mm");
            viewHolder.time.setText(sfd.format(new Date(timestamp)));

            Glide.with(context).load(ChatActivity.sImage).into(viewHolder.senderDP);

            if(messageModel.getReaction() != -1){
                viewHolder.reactionRight.setImageResource(reactions[messageModel.getReaction()]);
                viewHolder.reactionRight.setVisibility(View.VISIBLE);
            }
            else {
                viewHolder.reactionRight.setVisibility(View.GONE);
            }

        }else{
            receiverViewHolder viewHolder = (receiverViewHolder) holder;

            viewHolder.chatLayout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    viewHolder.chatLayout.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
                    mOnItemTouchListener.onInterceptTouchEvent(rv, event);
                    popup.onTouch(v, event);
                    return false;
                }
            });

            if(messageModel.getMessage() == null){
                Glide.with(context).load(messageModel.getChatImageId()).into(viewHolder.chatImageLeft);

                viewHolder.chatImageLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ZoomedChatImage.class);
                        intent.putExtra("ReceiverImage", messageModel.getChatImageId());
                        context.startActivity(intent);
                    }
                });
            }
            else{
                viewHolder.receiverID.setText(messageModel.getMessage());
            }

            Glide.with(context).load(ChatActivity.rImage).into(viewHolder.receiverDP);

            long timestamp = messageModel.getTimeStamp();
            SimpleDateFormat sfd = new SimpleDateFormat("hh:mm");
            viewHolder.time.setText(sfd.format(new Date(timestamp)));

            if(messageModel.getReaction() != -1){
                viewHolder.reactionLeft.setImageResource(reactions[messageModel.getReaction()]);
                viewHolder.reactionLeft.setVisibility(View.VISIBLE);
            }
            else {
                viewHolder.reactionLeft.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }


    @Override
    public int getItemViewType(int position) {
        MessageModel messageModel = arrayList.get(position);
        if(messageModel.getMessage() == null && FirebaseAuth.getInstance().getCurrentUser().getUid().equals(messageModel.getSenderID()))
            return ITEM_SEND_IMAGE;     // RIGHT
        else if(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(messageModel.getSenderID()))
            return ITEM_SEND;
        else if(messageModel.getMessage() == null && !FirebaseAuth.getInstance().getCurrentUser().getUid().equals(messageModel.getSenderID()))
            return ITEM_RECEIVE_IMAGE;   //LEFT
        else
            return ITEM_RECEIVE;
    }

    private void SetReaction(senderViewHolder viewHolder, int[] reactions, MessageModel messageModel) {
        FirebaseDatabase.getInstance().getReference().child("chats").child(senderRoom).child("message").child(messageModel.getMessageID()).child("reaction").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    int currPosition = snapshot.getValue(Integer.class);
                    if (currPosition != -1) {
                        Glide.with(context).load(reactions[currPosition]).into(viewHolder.reactionRight);
                    }
                    else {
                        viewHolder.reactionRight.setVisibility(View.GONE);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static class senderViewHolder extends RecyclerView.ViewHolder{
        CircleImageView senderDP;
        ImageView chatImageRight, reactionRight;
        LinearLayout rightChatID;
        TextView senderID, time;
        public senderViewHolder(@NonNull View itemView) {
            super(itemView);
            rightChatID = itemView.findViewById(R.id.rightChatID);
            chatImageRight = itemView.findViewById(R.id.chatImageRight);
            reactionRight = itemView.findViewById(R.id.reactionRight);
            senderID = itemView.findViewById(R.id.senderid);
            senderDP = itemView.findViewById(R.id.senderdp);
            time = itemView.findViewById(R.id.time);
        }
    }

    public static class receiverViewHolder extends RecyclerView.ViewHolder{
        CircleImageView receiverDP;
        TextView receiverID, time;
        ImageView chatImageLeft, reactionLeft;
        LinearLayout chatLayout;
        public receiverViewHolder(@NonNull View itemView) {
            super(itemView);
            chatImageLeft = itemView.findViewById(R.id.chatImageLeft);
            reactionLeft = itemView.findViewById(R.id.reactionLeft);
            chatLayout = itemView.findViewById(R.id.leftChatID);
            receiverDP = itemView.findViewById(R.id.receiverdp);
            receiverID = itemView.findViewById(R.id.receiverid);
            time = itemView.findViewById(R.id.time);
        }
    }

    private void DeleteMessage(MessageModel messageModel) {
        builder.setMessage("Do you want to permanently delete this message?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DatabaseReference senderChatReference = FirebaseDatabase.getInstance().getReference().child("chats").child(senderRoom).child("message");
                        DatabaseReference receiverChatReference = FirebaseDatabase.getInstance().getReference().child("chats").child(receiverRoom).child("message");

                        receiverChatReference.child(messageModel.getMessageID()).removeValue();
                        senderChatReference.child(messageModel.getMessageID()).removeValue();

                        Toast.makeText(context,"Deleted Successfully",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.setTitle("Delete Message");
        alert.show();
    }

    private ReactionPopup ReactionManager(int reactions[], RecyclerView.ViewHolder holder, MessageModel messageModel){

        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reactions)
                .build();

        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {
            if(pos == -1)
                return true;

            if(holder.getClass() == receiverViewHolder.class){
                receiverViewHolder viewHolder = (receiverViewHolder) holder;
                viewHolder.reactionLeft.setImageResource(reactions[pos]);
                viewHolder.reactionLeft.setVisibility(View.VISIBLE);
            }
            else if (holder.getClass() == senderViewHolder.class){
                senderViewHolder viewHolder = (senderViewHolder) holder;
                viewHolder.reactionRight.setImageResource(reactions[pos]);
                viewHolder.reactionRight.setVisibility(View.VISIBLE);
            }

            messageModel.setReaction(pos);

            HashMap<String, Object> map = new HashMap<>();

            map.put("reaction", pos);

            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(senderRoom)
                    .child("message")
                    .child(messageModel.getMessageID())
                    .updateChildren(map);

            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(receiverRoom)
                    .child("message")
                    .child(messageModel.getMessageID())
                    .updateChildren(map);
            notifyDataSetChanged();
            return true; // true is closing popup, false is requesting a new selection
        });

        return popup;
    }
}
