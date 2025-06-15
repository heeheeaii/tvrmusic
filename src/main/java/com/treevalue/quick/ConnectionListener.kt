package com.treevalue.quick


/**
 * A listener interface to receive notifications when a new neural connection is successfully formed.
 * 两个神经元之间连接成功回调
 */
fun interface ConnectionListener {
    /**
     * Called when a connection is established between two neurons after a growth segment completes.
     *
     * @param from The source neuron of the connection.
     * @param to The newly created target neuron.
     */
    fun onConnect(from: Neuron, to: Neuron)
}
