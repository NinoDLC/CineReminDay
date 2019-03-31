package fr.delcey.cinereminday.main;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

class RemindayUseCase extends BaseUseCase {

    private final SharedPreferences sharedPreferences;

    private MutableLiveData<List<RemindayMainCard>> _cards = new MutableLiveData<>();
    public LiveData<List<RemindayMainCard>> cards = _cards;

    private List<AsyncTask> asyncTasks = new ArrayList<>();

    public RemindayUseCase(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    void cancel() {
        for (AsyncTask asyncTask : asyncTasks) {
            asyncTask.cancel(true);
        }
    }

    public void loadCards() {
        AsyncTask<Void, Void, List<RemindayMainCard>> asyncTask = new CardsAsync(this);

        asyncTasks.add(asyncTask);
        asyncTask.execute();
    }

    private static class CardsAsync extends AsyncTask<Void, Void, List<RemindayMainCard>> {

        private WeakReference<RemindayUseCase> useCaseRef;

        public CardsAsync(RemindayUseCase useCase) {
            useCaseRef = new WeakReference<>(useCase);
        }

        @Override
        protected List<RemindayMainCard> doInBackground(Void... voids) {
            RemindayUseCase useCase = useCaseRef.get();

            if (useCase != null) {
                useCase.sharedPreferences.getString(); // WTF AM I EVEN DOING HERE
            }
        }
    }
}
