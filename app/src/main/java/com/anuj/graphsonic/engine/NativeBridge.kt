package com.anuj.graphsonic.engine

class NativeBridge{
    companion object{
        init {
            System.loadLibrary("graphsonic")
        }
    }
    external fun getEngineMessage(): String
}