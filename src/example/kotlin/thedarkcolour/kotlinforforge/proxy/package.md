# thedarkcolour.kotlinforforge.proxy
This package has example proxy classes. 
Proxies are used to provide common declarations with sided implementations.

Forge no longer supports the proxy pattern. 
The ``@SidedProxy`` annotation was removed in 1.13+. 
This example shows a use case for the ``lazySidedDelegate``.
It is recommended to use the ``runWhenOn`` and ``callWhenOn`` functions 
instead of proxies whenever possible.

In this example, a proxy is instantiated lazily in the ``ExampleMod`` class. 
Proxies are not the only use for sided delegates.