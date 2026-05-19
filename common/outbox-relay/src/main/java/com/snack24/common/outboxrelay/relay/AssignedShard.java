package com.snack24.common.outboxrelay.relay;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class AssignedShard {
    private final List<Long> shards;

    public static AssignedShard of(String selfAppId, List<String> appIds, long shardCount) {
        int index = appIds.indexOf(selfAppId);
        if (index == -1) {
            return new AssignedShard(List.of());
        }
        int totalApps = appIds.size();
        List<Long> shards = new ArrayList<>();
        for (long shard = index; shard < shardCount; shard += totalApps) {
            shards.add(shard);
        }
        return new AssignedShard(shards);
    }
}
