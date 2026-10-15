import { Group, Modal as MantineModal } from "@mantine/core";

export default function Modal({ title, onClose, children, footer }) {
  return (
    <MantineModal
      opened
      onClose={onClose}
      title={title}
      centered
      radius="lg"
      overlayProps={{ backgroundOpacity: 0.4, blur: 2 }}
    >
      {children}
      {footer ? (
        <Group justify="flex-end" mt="lg">
          {footer}
        </Group>
      ) : null}
    </MantineModal>
  );
}
