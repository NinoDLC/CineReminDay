package fr.delcey.cinereminday.main;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import fr.delcey.cinereminday.R;
import fr.delcey.cinereminday.main.recyclerview_viewmodel.CRDMainViewModel;
import java.util.List;

/**
 * Created by Nino on 09/09/2018.
 */

public class CRDMainAdapter
    extends Adapter<CRDMainAdapter.ViewHolder> {

    private List<CRDMainViewModel> mData;

    public CRDMainAdapter(Listener listener, List<CRDMainViewModel> data) {
        mData = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                                            .inflate(R.layout.main_recyclerview_item,
                                                     parent,
                                                     false));

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(View itemView) {
            super(itemView);
        }
    }

    public interface Listener {

        // Android SMS permission
        void onAskSmsPermissionButtonClicked();

        // Google policy : ask user permission to periodically send SMS
        void onScheduleSmsButtonClicked();

        // Status
        void onRetryButtonClicked(); // TODO VOLKO FIND BETTER NAME

        void onVisitStorePageButtonClicked();

    }
}
