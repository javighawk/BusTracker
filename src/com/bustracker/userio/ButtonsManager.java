package com.bustracker.userio;

import rx.Observable;

public interface ButtonsManager {
	
	Observable<UserInputEvent> getButtonsPressedEvents();

}
