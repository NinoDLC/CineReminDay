package fr.delcey.cinereminday.cloud_manager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import fr.delcey.cinereminday.CRDAuthActivity;
import fr.delcey.cinereminday.R;
import fr.delcey.cinereminday.cloud_manager.beans.CinedayCode;

/**
 * Created by Nino on 12/03/2017.
 */

public class CRDCodesActivity extends CRDAuthActivity implements View.OnClickListener {
    private DatabaseReference mFireDataBaseRef;

    private FirebaseRecyclerAdapter mAdapter;

    private Button mButtonAdd;
    private Button mButtonClear;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.cloudmanager_activity);

        mFireDataBaseRef = FirebaseDatabase.getInstance().getReference();

        mButtonAdd = (Button) findViewById(R.id.btn_add);
        mButtonClear = (Button) findViewById(R.id.btn_clear);

        mButtonAdd.setOnClickListener(this);
        mButtonClear.setOnClickListener(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_codes);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new FirebaseRecyclerAdapter<CinedayCode, CinedayCodeHolder>(CinedayCode.class, R.layout.cloudmanager_recyclerview_item, CinedayCodeHolder.class, mFireDataBaseRef) {
            @Override
            protected void populateViewHolder(CinedayCodeHolder viewHolder, CinedayCode model, int position) {
                viewHolder.setName(model.getCode());
                viewHolder.setText(String.valueOf(model.getTimestamp()));
                viewHolder.setAvailable(model.isAvailable());
            }
        };

        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mAdapter.cleanup();
    }

    @Override
    public void onClick(View v) {
        if (v == mButtonAdd) {
            onButtonAddClicked();
        } else if (v == mButtonClear) {
            onButtonClearClicked();
        }
    }

    private void onButtonAddClicked() {
        mFireDataBaseRef.push().setValue(new CinedayCode("WOLOLO", System.currentTimeMillis(), true));
    }

    private void onButtonClearClicked() {

    }

    @Override
    protected void onFirebaseUserSignedOut() {
        super.onFirebaseUserSignedOut();

        Toast.makeText(this, "You have been disconnected from Firebase !", Toast.LENGTH_LONG).show();

        finish();
    }

    @Override
    protected void onFirebaseConnectionFailed() {
        super.onFirebaseConnectionFailed();

        Toast.makeText(this, "You have been disconnected from Firebase !", Toast.LENGTH_LONG).show();

        finish();
    }

    // Firebase needs this to be public static
    public static class CinedayCodeHolder extends RecyclerView.ViewHolder {
        private final TextView mTvTimestamp;
        private final TextView mTvWololo;
        private final ImageView mIvAvailable;

        public CinedayCodeHolder(View itemView) {
            super(itemView);

            mTvTimestamp = (TextView) itemView.findViewById(R.id.cloudmanager_tv_timestamp);
            mTvWololo = (TextView) itemView.findViewById(R.id.cloudmanager_tv_code);
            mIvAvailable = (ImageView) itemView.findViewById(R.id.cloudmanager_iv_available);
        }

        public void setName(String name) {
            mTvTimestamp.setText(name);
        }

        public void setText(String text) {
            mTvWololo.setText(text);
        }

        public void setAvailable(boolean isAvailable) {
            if (isAvailable) {
                mIvAvailable.setImageResource(R.drawable.cloudmanager_ic_code_available);
            } else {
                mIvAvailable.setImageResource(R.drawable.cloudmanager_ic_code_unavailable);
            }
        }
    }
}
