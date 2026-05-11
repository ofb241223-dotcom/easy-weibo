async (page) => {
  await page.goto('http://127.0.0.1:5173/post/1');
  const buttons = await page.locator('main button').evaluateAll((els) => els.map((el, i) => ({ i, text: (el.textContent || '').trim() })).filter(x => x.text));
  return buttons.slice(0, 20);
}
