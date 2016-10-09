package services;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Solovyev on 05/10/16.
 */
public interface LikeService {
    /*Я изменил сигнатуру функции по нескольким причинам:
      -Нам нужно не допускать двойных лайков. Сделать это можно только запомнив кто кого лайкал
      -Id игрока который лайкает - это легкодоступная информация. Код не придется менять сильно
      -Это позволяет переложить проблемы потокобезопасности на базу(гораздо безопаснее чем вариант со счетчиком)
    */
    void like(@NotNull String initiator, @NotNull String target);
    long getLikes(@NotNull String playerId);
}
