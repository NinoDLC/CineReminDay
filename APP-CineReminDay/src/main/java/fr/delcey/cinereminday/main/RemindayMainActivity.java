package fr.delcey.cinereminday.main;

import android.os.Bundle;
import android.os.PersistableBundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import fr.delcey.cinereminday.R;
import java.util.List;

public class RemindayMainActivity extends AppCompatActivity implements RemindayAdapter.Callbacks {

    private RemindayMainViewModel model;

    private RemindayAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

        setContentView(R.layout.reminday_main_activity);

        RecyclerView recyclerView = findViewById(R.id.reminday_main_activity);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RemindayAdapter(this);
        recyclerView.setAdapter(adapter);

        model = ViewModelProviders.of(this, RemindayViewModelFactory.getInstance()).get(RemindayMainViewModel.class);
        model.cards.observe(this, new Observer<List<RemindayMainCard>>() {
            @Override
            public void onChanged(List<RemindayMainCard> remindayMainCard) {
                adapter.submitList(remindayMainCard);
            }
        });
    }

    @Override
    public void onShouldDisplayPermissionPopUp() {
        //model.displayPermissionPopUp();
    }

    @Override
    public void onUserOkWithSmsSending() {
        //model.setupSmsSending();
    }

    @Override
    public void onShareCodeWithWorld() {
        //model.shareCodeWithWorld();
    }

    @Override
    public void onShareCodeWithFriend() {
        //model.shareCodeWithFriend();
    }

    @Override
    public void onAskForCodeFromWorld() {
        //model.askCodeFromWorld();
    }

    @Override
    public void onRetrySmsSending() {
        //model.retrySmsSending();
    }
}
