package com.arthlimchiu.firebasechat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.ExecutionException;

public class CreateRoomActivity extends AppCompatActivity {

    private ChatRoomRepository chatRoomRepository;

    private EditText roomName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);

        chatRoomRepository = new ChatRoomRepository(FirebaseFirestore.getInstance());

        roomName = findViewById(R.id.room_name);

        setTitle(getString(R.string.create_room));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.create_room_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.create_room:
                if (isRoomEmpty()) {
                    Toast.makeText(this, getString(R.string.error_empty_room), Toast.LENGTH_SHORT).show();
                } else {
                    createRoom();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isRoomEmpty() {
        return roomName.getText().toString().isEmpty();
    }

    private void createRoom() {
        chatRoomRepository.createRoom(
                roomName.getText().toString(),
                new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Intent intent = new Intent(CreateRoomActivity.this, ChatRoomActivity.class);
                        intent.putExtra(ChatRoomActivity.CHAT_ROOM_ID, documentReference.getId());
                        intent.putExtra(ChatRoomActivity.CHAT_ROOM_NAME, roomName.getText().toString());
                        startActivity(intent);
                        finish();
                    }
                },
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(
                                CreateRoomActivity.this,
                                getString(R.string.error_creating_room),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }
}
