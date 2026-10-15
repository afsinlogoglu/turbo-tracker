export default function AppShell({ children }) {
  return (
    <div className="canvas">
      <div className="window">{children}</div>
    </div>
  );
}
