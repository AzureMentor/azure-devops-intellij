// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.plugin.idea.tfvc.core;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.microsoft.alm.plugin.authentication.AuthenticationInfo;
import com.microsoft.alm.plugin.context.ServerContext;
import com.microsoft.alm.plugin.external.models.PendingChange;
import com.microsoft.alm.plugin.external.reactive.ReactiveTfvcClientHolder;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * The new, "reactive" TFVC client implementation. Most of the methods are asynchronous and delegate to the external
 * client that is always running. Works with Rd protocol.
 */
public class ReactiveTfvcClient implements TfvcClient {

    private static Logger ourLogger = Logger.getInstance(ReactiveTfvcClient.class);

    @NotNull
    private final Project myProject;

    public ReactiveTfvcClient(@NotNull Project project) {
        myProject = project;
    }

    @Override
    @NotNull
    public CompletableFuture<List<PendingChange>> getStatusForFilesAsync(
            @NotNull ServerContext serverContext,
            @NotNull List<String> pathsToProcess) {
        long startTime = System.nanoTime();

        URI collectionUri = serverContext.getCollectionURI();
        AuthenticationInfo authenticationInfo = serverContext.getAuthenticationInfo();
        Stream<Path> paths = pathsToProcess.stream().map(Paths::get);

        return ReactiveTfvcClientHolder.getInstance(myProject).getClient()
                .thenCompose(client -> client.getPendingChangesAsync(collectionUri, authenticationInfo, paths))
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        long endTime = System.nanoTime();
                        double seconds = ((double) endTime - startTime) / 1_000_000_000.0;
                        ourLogger.info("Status command successfully executed in " + seconds + " sec");
                    }
                });
    }
}
