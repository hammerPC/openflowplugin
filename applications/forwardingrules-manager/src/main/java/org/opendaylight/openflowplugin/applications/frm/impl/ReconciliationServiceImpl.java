/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm.impl;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import javax.annotation.Nullable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.ReconcileNodeInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.ReconcileNodeOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.ReconcileNodeOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev180227.ReconciliationService;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReconciliationServiceImpl implements ReconciliationService {

    private static final Logger LOG = LoggerFactory.getLogger(ReconciliationServiceImpl.class);

    private final ForwardingRulesManagerImpl forwardingRulesManagerImpl;

    public ReconciliationServiceImpl(ForwardingRulesManagerImpl forwardingRulesManagerImpl) {
        this.forwardingRulesManagerImpl = forwardingRulesManagerImpl;
    }

    private Node buildNode(long nodeIid) {
        NodeId nodeId = new NodeId("openflow:" + nodeIid);
        Node nodeDpn = new NodeBuilder().setId(nodeId).withKey(new NodeKey(nodeId)).build();
        return nodeDpn;
    }

    @Override
    public ListenableFuture<RpcResult<ReconcileNodeOutput>> reconcileNode(ReconcileNodeInput input) {
        LOG.debug("Triggering reconciliation for node: {}", input.getNodeId().toString());
        Node nodeDpn = buildNode(input.getNodeId().longValue());
        InstanceIdentifier<FlowCapableNode> connectedNode = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, nodeDpn.key()).augmentation(FlowCapableNode.class).build();
        SettableFuture<RpcResult<ReconcileNodeOutput>> rpcResult = SettableFuture.create();
        ListenableFuture<Boolean> futureResult = forwardingRulesManagerImpl
                .getNodeListener().reconcileConfiguration(connectedNode);
        Futures.addCallback(futureResult, new ResultCallBack(futureResult, rpcResult));
        LOG.debug("Completing reconciliation for node: {}", input.getNodeId().toString());
        return rpcResult;
    }

    private static class ResultCallBack implements FutureCallback<Boolean> {
        private final SettableFuture<RpcResult<ReconcileNodeOutput>> futureResult;

        ResultCallBack(ListenableFuture<Boolean> rpcResult,
                       SettableFuture<RpcResult<ReconcileNodeOutput>> futureResult) {
            this.futureResult = futureResult;
        }

        @Override
        public void onSuccess(@Nullable Boolean result) {
            ReconcileNodeOutput output = new ReconcileNodeOutputBuilder().setResult(result).build();
            futureResult.set(RpcResultBuilder.success(output).build());
        }

        @Override
        public void onFailure(Throwable error) {
            LOG.error("initReconciliation failed", error);
            ReconcileNodeOutput output = new ReconcileNodeOutputBuilder().setResult(false).build();
            futureResult.set(RpcResultBuilder.success(output).build());
        }
    }
}
