import { useState } from "react";
import { Alert, Button, Paper, PasswordInput, SegmentedControl, Stack, Text, TextInput, Title } from "@mantine/core";
import { login, register, saveSession } from "../api";

export default function AuthPage({ onSuccess }) {
  const [mode, setMode] = useState("login");
  const [name, setName] = useState("");
  const [email, setEmail] = useState("ada@turbo.dev");
  const [password, setPassword] = useState("password");
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);

  async function submit(event) {
    event.preventDefault();
    setSaving(true);
    setError("");
    try {
      const auth =
        mode === "login"
          ? await login({ email: email.trim(), password })
          : await register({ name: name.trim(), email: email.trim(), password });
      saveSession(auth);
      onSuccess(auth.user);
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  }

  return (
    <main className="auth">
      <Paper className="auth-card" component="form" onSubmit={submit} radius="lg" p="xl" shadow="md" withBorder>
        <div className="brand">
          <span className="logo" aria-hidden="true">
            <i />
            <i />
            <i />
            <i />
          </span>
          <strong>turbo</strong>
        </div>
        <Title order={2} mt="md">
          {mode === "login" ? "Sign in" : "Create account"}
        </Title>
        <Text c="dimmed" size="sm" mt={4}>
          Demo admin: ada@turbo.dev / password
        </Text>
        <SegmentedControl
          mt="md"
          fullWidth
          value={mode}
          onChange={setMode}
          data={[
            { label: "Sign in", value: "login" },
            { label: "Register", value: "register" },
          ]}
        />
        <Stack mt="md" gap="sm">
          {mode === "register" ? (
            <TextInput label="Name" value={name} onChange={(event) => setName(event.target.value)} required />
          ) : null}
          <TextInput label="Email" type="email" value={email} onChange={(event) => setEmail(event.target.value)} required />
          <PasswordInput label="Password" value={password} onChange={(event) => setPassword(event.target.value)} minLength={8} required />
          {error ? (
            <Alert color="red" variant="light">
              {error}
            </Alert>
          ) : null}
          <Button type="submit" loading={saving} fullWidth>
            {mode === "login" ? "Sign in" : "Create account"}
          </Button>
        </Stack>
      </Paper>
    </main>
  );
}
