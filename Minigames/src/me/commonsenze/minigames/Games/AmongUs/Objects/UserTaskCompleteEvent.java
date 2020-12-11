package me.commonsenze.minigames.Games.AmongUs.Objects;

import lombok.Getter;
import me.commonsenze.minigames.Objects.User;
import me.commonsenze.minigames.Util.UserEvent;

public class UserTaskCompleteEvent extends UserEvent {

	@Getter private Task task;
	
	public UserTaskCompleteEvent(User user, Task task) {
		super(user);
		this.task = task;
	}

}
