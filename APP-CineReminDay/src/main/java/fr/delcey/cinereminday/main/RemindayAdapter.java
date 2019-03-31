package fr.delcey.cinereminday.main;

import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil.ItemCallback;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

class RemindayAdapter extends ListAdapter<RemindayMainCard, RemindayAdapter.ViewHolder> {

    private final Callbacks callbacks;

    protected RemindayAdapter(Callbacks callbacks) {
        super(new DiffCallback());

        this.callbacks = callbacks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    }

    public interface Callbacks {

        void onShouldDisplayPermissionPopUp();

        void onUserOkWithSmsSending();

        void onShareCodeWithWorld();

        void onShareCodeWithFriend();

        void onAskForCodeFromWorld();

        void onRetrySmsSending();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    private static class DiffCallback extends ItemCallback<RemindayMainCard> {

        @Override
        public boolean areItemsTheSame(@NonNull RemindayMainCard oldItem, @NonNull RemindayMainCard newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull RemindayMainCard oldItem, @NonNull RemindayMainCard newItem) {
            return oldItem.equals(newItem);
        }
    }
}
