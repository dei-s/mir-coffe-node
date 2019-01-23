package mir.coffe.lang

import mir.coffe.lang.v1.BaseGlobal

package object hacks {
  private[lang] val Global: BaseGlobal = mir.coffe.lang.Global // Hack for IDEA
}
