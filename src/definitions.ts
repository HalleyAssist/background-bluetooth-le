export interface BackgroundBLEPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
