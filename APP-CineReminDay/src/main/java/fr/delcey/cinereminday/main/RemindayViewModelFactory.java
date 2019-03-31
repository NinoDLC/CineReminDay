package fr.delcey.cinereminday.main;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class RemindayViewModelFactory implements ViewModelProvider.Factory {

    private static volatile RemindayViewModelFactory remindayViewModelFactory;

    private RemindayViewModelFactory() {

    }

    public static RemindayViewModelFactory getInstance() {
        if (RemindayViewModelFactory.remindayViewModelFactory == null) {
            synchronized (RemindayViewModelFactory.class) {
                if (RemindayViewModelFactory.remindayViewModelFactory == null) {
                    RemindayViewModelFactory.remindayViewModelFactory = new RemindayViewModelFactory();
                }
            }
        }

        return RemindayViewModelFactory.remindayViewModelFactory;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(RemindayMainViewModel.class)) {
            return (T) new RemindayMainViewModel(new RemindayUseCase());
        }

        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
