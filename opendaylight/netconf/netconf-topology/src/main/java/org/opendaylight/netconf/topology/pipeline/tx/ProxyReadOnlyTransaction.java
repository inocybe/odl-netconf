/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.netconf.topology.pipeline.tx;

import akka.actor.ActorSystem;
import akka.dispatch.OnComplete;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import javax.annotation.Nullable;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.dom.api.DOMDataReadOnlyTransaction;
import org.opendaylight.netconf.sal.connect.util.RemoteDeviceId;
import org.opendaylight.netconf.topology.pipeline.ProxyNetconfDeviceDataBroker;
import org.opendaylight.netconf.topology.util.messages.NormalizedNodeMessage;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import scala.concurrent.Future;

public class ProxyReadOnlyTransaction implements DOMDataReadOnlyTransaction{

    private final RemoteDeviceId id;
    private final ProxyNetconfDeviceDataBroker delegate;
    private final ActorSystem actorSystem;

    public ProxyReadOnlyTransaction(final ActorSystem actorSystem, final RemoteDeviceId id, final ProxyNetconfDeviceDataBroker delegate) {
        this.id = id;
        this.delegate = delegate;
        this.actorSystem = actorSystem;
    }

    @Override
    public void close() {
        //NOOP
    }

    @Override
    public CheckedFuture<Optional<NormalizedNode<?, ?>>, ReadFailedException> read(final LogicalDatastoreType store, final YangInstanceIdentifier path) {
        final Future<Optional<NormalizedNodeMessage>> future = delegate.read(store, path);
        final SettableFuture<Optional<NormalizedNode<?, ?>>> settableFuture = SettableFuture.create();
        final CheckedFuture<Optional<NormalizedNode<?, ?>>, ReadFailedException> checkedFuture = Futures.makeChecked(settableFuture, new Function<Exception, ReadFailedException>() {
            @Nullable
            @Override
            public ReadFailedException apply(Exception cause) {
                return new ReadFailedException("Read from transaction failed", cause);
            }
        });
        future.onComplete(new OnComplete<Optional<NormalizedNodeMessage>>() {
            @Override
            public void onComplete(Throwable throwable, Optional<NormalizedNodeMessage> normalizedNodeMessage) throws Throwable {
                if (throwable == null) {
                    settableFuture.set(normalizedNodeMessage.transform(new Function<NormalizedNodeMessage, NormalizedNode<?, ?>>() {
                        @Nullable
                        @Override
                        public NormalizedNode<?, ?> apply(NormalizedNodeMessage input) {
                            return input.getNode();
                        }
                    }));
                } else {
                    settableFuture.setException(throwable);
                }
            }
        }, actorSystem.dispatcher());
        return checkedFuture;
    }

    @Override
    public CheckedFuture<Boolean, ReadFailedException> exists(final LogicalDatastoreType store, final YangInstanceIdentifier path) {
        final Future<Boolean> existsFuture = delegate.exists(store, path);
        final SettableFuture<Boolean> settableFuture = SettableFuture.create();
        final CheckedFuture<Boolean, ReadFailedException> checkedFuture = Futures.makeChecked(settableFuture, new Function<Exception, ReadFailedException>() {
            @Nullable
            @Override
            public ReadFailedException apply(Exception cause) {
                return new ReadFailedException("Read from transaction failed", cause);
            }
        });
        existsFuture.onComplete(new OnComplete<Boolean>() {
            @Override
            public void onComplete(Throwable throwable, Boolean result) throws Throwable {
                if (throwable == null) {
                    settableFuture.set(result);
                } else {
                    settableFuture.setException(throwable);
                }
            }
        }, actorSystem.dispatcher());
        return checkedFuture;
    }

    @Override
    public Object getIdentifier() {
        return this;
    }
}
