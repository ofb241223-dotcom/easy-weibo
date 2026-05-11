async (page) => {
  await page.goto('http://127.0.0.1:5173/chat?conversation=1');
  await page.getByText('HNUST Admin').nth(0).click().catch(() => {});
  await page.waitForTimeout(600);
  return { url: page.url(), title: await page.title() };
}
