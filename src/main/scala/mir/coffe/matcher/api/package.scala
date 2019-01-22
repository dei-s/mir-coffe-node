package mir.coffe.matcher
import mir.coffe.matcher.model.{LevelAgg, LimitOrder}
import mir.coffe.matcher.model.MatcherModel.{Level, Price}

package object api {
  def aggregateLevel(l: (Price, Level[LimitOrder])) = LevelAgg(l._2.view.map(_.amount).sum, l._1)
}
