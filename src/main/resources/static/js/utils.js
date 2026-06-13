/**
 * Shared front-end helpers, available on every page via base.html.
 */

/**
 * Grows every card in the given collection to the height of the tallest one, so that
 * cards in a wrapping grid line up regardless of how much content each holds.
 * The inline height is cleared before measuring, so the function is safe to re-run
 * after the content changes (e.g. on resize or after a partial swap).
 *
 * @param {Iterable<HTMLElement>} cards the card elements to equalize
 */
function equalizeCardHeights(cards) {
    const list = Array.from(cards);
    list.forEach(card => { card.style.height = ''; });
    let maxHeight = 0;
    list.forEach(card => { maxHeight = Math.max(maxHeight, card.offsetHeight); });
    list.forEach(card => { card.style.height = maxHeight + 'px'; });
}