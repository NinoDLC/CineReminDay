package fr.delcey.cinereminday.main;

import androidx.lifecycle.LiveData;
import java.util.List;

class RemindayMainViewModel extends BaseViewModel {

    private RemindayUseCase useCase;

    public LiveData<List<RemindayMainCard>> cards;

    public RemindayMainViewModel(RemindayUseCase useCase) {
        super(useCase);

        this.useCase = useCase;
        cards = useCase.cards;

        loadCards();
    }

    private void loadCards() {
        useCase.loadCards();
    }
}
