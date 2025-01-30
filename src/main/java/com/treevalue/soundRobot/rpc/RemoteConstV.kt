package com.treevalue.soundRobot.rpc

object RemoteConstV {
    object Reduction {
        private const val ID = "reduction"
        var nearby = "nearby/"
            get() {
                return "${ID}/${field}";
            }

        var fromTo = "fromTo/"
            get() {
                return "${ID}/${field}";
            }

        var save = "save/"
            get() {
                return "${ID}/${field}";
            }
    }
}
