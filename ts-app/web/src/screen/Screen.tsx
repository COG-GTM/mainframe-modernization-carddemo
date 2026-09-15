import type { ReactNode } from 'react';

/** BMS DFHMDF COLOR= values. */
export type Color = 'blue' | 'yellow' | 'turquoise' | 'green' | 'red' | 'neutral';

/**
 * A 24x80 3270 screen: children are placed by BMS POS=(line,column).
 * ENTER submits, as it does on the terminal.
 */
export function Screen({
  children,
  onEnter,
}: {
  children: ReactNode;
  onEnter?: () => void;
}): JSX.Element {
  return (
    <form
      className="screen"
      onSubmit={(event) => {
        event.preventDefault();
        onEnter?.();
      }}
    >
      {children}
      <button type="submit" className="enter">
        ENTER
      </button>
    </form>
  );
}

interface Positioned {
  /** BMS POS line, 1-based. */
  row: number;
  /** BMS POS column, 1-based. */
  col: number;
  /** BMS LENGTH. */
  len?: number | undefined;
  color?: Color | undefined;
}

function cellStyle({ row, col, len }: Positioned): React.CSSProperties {
  return { gridRow: row, gridColumn: `${col} / span ${Math.max(len ?? 1, 1)}` };
}

export function Text({
  row,
  col,
  len,
  color = 'blue',
  children,
  bright = false,
  underline = false,
  align = 'left',
}: Positioned & {
  children: ReactNode;
  bright?: boolean;
  underline?: boolean;
  align?: 'left' | 'right';
}): JSX.Element {
  const classes = ['field', color];
  if (bright) classes.push('bright');
  if (underline) classes.push('underline');
  if (align === 'right') classes.push('right');
  return (
    <span className={classes.join(' ')} style={cellStyle({ row, col, len })}>
      {children}
    </span>
  );
}

export function Input({
  row,
  col,
  len = 1,
  color = 'green',
  value,
  onChange,
  id,
  label,
  autoFocus = false,
  numeric = false,
  password = false,
}: Positioned & {
  value: string;
  onChange: (value: string) => void;
  id: string;
  label: string;
  autoFocus?: boolean;
  numeric?: boolean;
  password?: boolean;
}): JSX.Element {
  return (
    <input
      id={id}
      aria-label={label}
      className={`field input ${color}`}
      style={cellStyle({ row, col, len })}
      type={password ? 'password' : 'text'}
      inputMode={numeric ? 'numeric' : 'text'}
      maxLength={len}
      size={len}
      value={value}
      autoFocus={autoFocus}
      autoComplete="off"
      onChange={(event) => onChange(event.target.value)}
    />
  );
}
