package fr.perrier.guard.network;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Message Guard transporté sur le canal : un type (1=staffchat, 2=notify, 3=vanish) + un payload libre. */
@Getter
@RequiredArgsConstructor
public class GuardFrame {
    private final byte type;
    private final String payload;
}
