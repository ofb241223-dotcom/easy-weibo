async (page) => {
  await page.goto('http://127.0.0.1:5173/post/1');
  const card = page.locator('main').locator('div,article').filter({ hasText: '@admin' }).first();
  const viewButton = card.getByRole('button').nth(4);
  await viewButton.click();
  await page.waitForTimeout(300);
  const bodyText = await page.textContent('body');
  return { hasDrawer: bodyText?.includes('浏览记录 ·') || false };
}
