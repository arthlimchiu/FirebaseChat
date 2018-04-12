package com.arthlimchiu.firebasechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String CURRENT_USER_KEY = "CURRENT_USER_KEY";

    AuthenticationRepository authentication;
    ChatRoomRepository chatRoomRepository;

    private FloatingActionButton createRoom;
    private RecyclerView chatRooms;
    private ChatRoomsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authentication = new AuthenticationRepository(FirebaseFirestore.getInstance());
        chatRoomRepository = new ChatRoomRepository(FirebaseFirestore.getInstance());

        createRoom = findViewById(R.id.create_room);

        initUI();

        getChatRooms();

        authenticate();
    }

    private void initUI() {
        chatRooms = findViewById(R.id.rooms);
        chatRooms.setLayoutManager(new LinearLayoutManager(this));

        createRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MainActivity", "Launch create a room screen");
                Intent intent = new Intent(MainActivity.this, CreateRoomActivity.class);
                startActivity(intent);
            }
        });
    }

    private void getChatRooms() {
        chatRoomRepository.getRooms(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot snapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e("MainActivity", "Listen failed.", e);
                    return;
                }

                List<ChatRoom> rooms = new ArrayList<>();
                for (QueryDocumentSnapshot doc : snapshots) {
                    rooms.add(new ChatRoom(doc.getId(), doc.getString("name")));
                }

                adapter = new ChatRoomsAdapter(rooms, listener);
                chatRooms.setAdapter(adapter);
            }
        });
    }

    ChatRoomsAdapter.OnChatRoomClickListener listener = new ChatRoomsAdapter.OnChatRoomClickListener() {
        @Override
        public void onClick(ChatRoom chatRoom) {
            Intent intent = new Intent(MainActivity.this, ChatRoomActivity.class);
            intent.putExtra(ChatRoomActivity.CHAT_ROOM_ID, chatRoom.getId());
            intent.putExtra(ChatRoomActivity.CHAT_ROOM_NAME, chatRoom.getName());
            startActivity(intent);
        }
    };

    private void authenticate() {
        String currentUserKey = getCurrentUserKey();
        if (currentUserKey.isEmpty()) {
            authentication.createNewUser(
                    new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            saveCurrentUserKey(documentReference.getId());
                            Toast.makeText(MainActivity.this, "New user created", Toast.LENGTH_SHORT).show();
                        }
                    },
                    new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(
                                    MainActivity.this,
                                    "Error creating user. Check your internet connection",
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
            );
        } else {
            authentication.login(
                    currentUserKey,
                    new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Toast.makeText(MainActivity.this, "Logged in", Toast.LENGTH_SHORT).show();
                        }
                    },
                    new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(
                                    MainActivity.this,
                                    "Error signing in. Check your internet connection",
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
            );
        }
    }

    private String getCurrentUserKey() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getString(CURRENT_USER_KEY, "");
    }

    private void saveCurrentUserKey(String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(CURRENT_USER_KEY, key);
        editor.apply();
    }
}
