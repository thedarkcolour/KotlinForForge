package thedarkcolour.kotlinforforge.proxy

/**
 * Common inheritor of both proxies.
 */
interface IProxy {
    fun modConstruction()
}

class ClientProxy : IProxy {
    override fun modConstruction() {
        // run client code
    }
}

class ServerProxy : IProxy {
    override fun modConstruction() {
        // run server code
    }
}