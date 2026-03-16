package services;

public class ConsoleTagObserver implements TagObserver {

    @Override
    public void onTagChanged(TagEvent event) {
        System.out.printf("[TAG] owner=%s contact=%s event=%s tag=%s at=%s%n",
                event.ownerId(),
                event.contactId(),
                event.type(),
                event.tag().getDisplay(),
                event.at());
    }
}
