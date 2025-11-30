import React, { useState, forwardRef, useImperativeHandle } from 'react';
import { useEditor, EditorContent } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import Placeholder from '@tiptap/extension-placeholder';
import { Bold, Italic, Strikethrough, List, ListOrdered, Link as LinkIcon } from 'lucide-react';

interface ContentEditorProps {
    asunto: string | null;
    cuerpo: string | null;
    onAsuntoChange?: (asunto: string) => void;
}

export interface ContentEditorHandle {
    getHTML: () => string;
}

export const ContentEditor = forwardRef<ContentEditorHandle, ContentEditorProps>(({
    asunto,
    cuerpo,
    onAsuntoChange
}, ref) => {
    const [localAsunto, setLocalAsunto] = useState(asunto || '');

    const handleAsuntoChange = (value: string) => {
        setLocalAsunto(value);
        onAsuntoChange?.(value);
    };

    const editor = useEditor({
        extensions: [
            StarterKit.configure({
                paragraph: {
                    HTMLAttributes: {
                        class: 'text-gray-800 text-base leading-relaxed py-1'
                    }
                },
                heading: {
                    HTMLAttributes: {
                        class: 'text-gray-900 font-bold mt-4 mb-2'
                    }
                },
                bulletList: {
                    HTMLAttributes: {
                        class: 'list-disc list-inside text-gray-800 my-2'
                    }
                },
                orderedList: {
                    HTMLAttributes: {
                        class: 'list-decimal list-inside text-gray-800 my-2'
                    }
                },
                listItem: {
                    HTMLAttributes: {
                        class: 'text-gray-800 py-0.5'
                    }
                }
            }),
            Placeholder.configure({
                placeholder: 'Escribe el contenido de tu correo aquí...'
            })
        ],
        content: cuerpo || '',
        editorProps: {
            attributes: {
                class: 'prose prose-sm prose-blue focus:outline-none max-w-none px-4 py-3 text-gray-800 min-h-64'
            }
        }
    });

    // Exponer el método getHTML al padre
    useImperativeHandle(ref, () => ({
        getHTML: () => editor?.getHTML() || ''
    }), [editor]);

    if (!editor) {
        return null;
    }

    const ToolbarButton = ({ 
        onClick, 
        isActive, 
        icon: Icon, 
        title,
        label,
        disabled = false
    }: { 
        onClick: () => void; 
        isActive: boolean; 
        icon: React.ComponentType<any>; 
        title: string;
        label: string;
        disabled?: boolean;
    }) => (
        <button
            type="button"
            onClick={onClick}
            disabled={disabled}
            title={title}
            className={`px-3 py-2 rounded-lg transition font-medium flex items-center gap-2 text-sm whitespace-nowrap ${
                isActive 
                    ? 'bg-primary text-white shadow-md' 
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
            } ${disabled ? 'opacity-50 cursor-not-allowed' : ''}`}
        >
            <Icon size={18} strokeWidth={2} />
            <span>{label}</span>
        </button>
    );

    return (
        <div className="space-y-6">
            {/* Asunto */}
            <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-100">
                <label className="block text-sm font-medium text-dark mb-3">
                    Asunto
                </label>
                <input
                    type="text"
                    value={localAsunto}
                    onChange={(e) => handleAsuntoChange(e.target.value)}
                    placeholder="Ej: ¡Novedades de Verano que te encantarán!"
                    className="w-full px-4 py-2 border border-gray-200 rounded-lg focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary/20"
                />
            </div>

            {/* Cuerpo del Correo */}
            <div className="bg-white rounded-lg shadow-sm border border-gray-100 overflow-hidden">
                <div className="p-6">
                    <label className="block text-sm font-medium text-dark mb-4">
                        Cuerpo del Correo
                    </label>
                    
                    {/* Toolbar de formato */}
                    <div className="mb-4 pb-4 border-b border-gray-200">
                        <p className="text-xs font-semibold text-gray-600 mb-3 uppercase tracking-wide">Opciones de formato</p>
                        <div className="flex gap-2 flex-wrap bg-gray-50 p-3 rounded-lg">
                            {/* Grupo: Texto */}
                            <div className="flex gap-2 items-center">
                                <span className="text-xs text-gray-500 font-semibold">Texto:</span>
                                <ToolbarButton
                                    onClick={() => { editor.chain().focus().toggleBold().run(); }}
                                    isActive={editor.isActive('bold')}
                                    icon={Bold}
                                    title="Negrita (Ctrl+B)"
                                    label="Negrita"
                                />
                                <ToolbarButton
                                    onClick={() => { editor.chain().focus().toggleItalic().run(); }}
                                    isActive={editor.isActive('italic')}
                                    icon={Italic}
                                    title="Itálica (Ctrl+I)"
                                    label="Itálica"
                                />
                                <ToolbarButton
                                    onClick={() => { editor.chain().focus().toggleStrike().run(); }}
                                    isActive={editor.isActive('strike')}
                                    icon={Strikethrough}
                                    title="Tachado"
                                    label="Tachado"
                                />
                            </div>
                            
                            <div className="w-px bg-gray-300"></div>
                            
                            {/* Grupo: Listas */}
                            <div className="flex gap-2 items-center">
                                <span className="text-xs text-gray-500 font-semibold">Listas:</span>
                                <ToolbarButton
                                    onClick={() => { editor.chain().focus().toggleBulletList().run(); }}
                                    isActive={editor.isActive('bulletList')}
                                    icon={List}
                                    title="Lista con viñetas (Ctrl+Shift+8)"
                                    label="Viñetas"
                                />
                                <ToolbarButton
                                    onClick={() => { editor.chain().focus().toggleOrderedList().run(); }}
                                    isActive={editor.isActive('orderedList')}
                                    icon={ListOrdered}
                                    title="Lista numerada (Ctrl+Shift+7)"
                                    label="Numerada"
                                />
                            </div>
                            
                            <div className="w-px bg-gray-300"></div>
                            
                            {/* Grupo: Enlaces */}
                            <div className="flex gap-2 items-center">
                                <span className="text-xs text-gray-500 font-semibold">Enlace:</span>
                                <ToolbarButton
                                    onClick={() => {
                                        const url = window.prompt('Ingresa la URL del enlace:\n\nEjemplo: https://ejemplo.com');
                                        if (url) {
                                            editor.chain().focus().extendMarkRange('link').setLink({ href: url }).run();
                                        }
                                    }}
                                    isActive={editor.isActive('link')}
                                    icon={LinkIcon}
                                    title="Insertar enlace (selecciona texto primero)"
                                    label="Añadir"
                                />
                            </div>
                        </div>
                        <p className="text-xs text-gray-500 mt-2">💡 Selecciona el texto y usa los botones para aplicar formato</p>
                    </div>

                    {/* Editor de texto */}
                    <div className="border border-gray-200 rounded-lg overflow-hidden bg-white">
                        <EditorContent 
                            editor={editor}
                        />
                    </div>
                </div>
            </div>
        </div>
    );
});

ContentEditor.displayName = 'ContentEditor';
